pipeline{
  agent none
  triggers {
    pollSCM('H/2 * * * *')
  }
  stages{
    stage('Build'){
      agent{node{label('ios')}}
      steps{
        lock('buildResource')
        {
          script{
            try{
              deleteDir()
              git(branch: 'release', credentialsId: 'ca110ba7-8385-4443-9376-dd4db46625c8', url: 'git@172.16.0.151:app/android2.git', poll: true)
              sh('''
                export PATH="/usr/local/bin/:$PATH"
                export LC_ALL=en_US.UTF-8
                export LANG=en_US.UTF-8
                curl http://api.fir.im/apps/latest/5c048efcca87a826b0c07ece?api_token=b34226983b0b4281c9efad321204ea12 > info.json
                export ANDROID_HOME=/Users/admin/Library/Android/sdk
                echo $ANDROID_HOME
                mkdir -p build
                fastlane releaseEnv
                ''') 
              stash(includes: 'app/build/outputs/apk/**/app-universal-*.apk', name: 'apk')
            }catch(e){
              def stageName = 'build'
              echo "R ${currentBuild.result} C ${currentBuild.currentResult}"
              if(currentBuild.currentResult == "FAILURE"){
                NotifyBuild(currentBuild.result, stageName)
              }
              currentBuild.result = "FAILURE"
              throw e
            }
          }
        }
      }
    }
    stage('Release') {
      agent{node{label('ios')}}
      when{
        not{equals(expected:"FAILURE", actual:currentBuild.result)}
      }
      steps{
        script{
          try{
            milestone("${env.BUILD_NUMBER}".toInteger())
            unstash(name: 'apk')
            sh('''
              export ANDROID_HOME=/Users/admin/Library/Android/sdk
              export apk_path=app/build/outputs/apk/release/
              mkdir -p release
              cd $apk_path
              apk=`ls *universal*`
              cd $WORKSPACE
              version=`$ANDROID_HOME/build-tools/28.0.3/aapt dump badging $apk_path$apk | grep versionName | awk '{print $4}' | sed s/versionName=//g | sed s/\\'//g`
              name=`$ANDROID_HOME/build-tools/28.0.3/aapt dump badging $apk_path$apk | grep application: | awk '{print $2}' | sed s/label=//g | sed s/\\'//g`
              icon=`$ANDROID_HOME/build-tools/28.0.3/aapt dump badging $apk_path$apk | grep application: | awk '{print $3}' | sed s/icon=//g | sed s/\\'//g`
              echo name=$name >> version.txt
              echo version=$version >> version.txt
              cp version.txt release
              unzip $apk_path$apk -d ./apk
              cp apk/$icon release/logo.png
              java -jar 360jiagu/jiagu/jiagu.jar -login lukai@sunmi.com sunmi388
              java -jar 360jiagu/jiagu/jiagu.jar -importsign Keystore.jks SUNMIwireless388 SUNMI_Key SUNMIwireless388
              java -jar 360jiagu/jiagu/jiagu.jar -jiagu $apk_path$apk release/ -autosign
              zip -jv release/$version.zip release/*
              rm -rf latest
              ''')
            archiveArtifacts(artifacts: 'release/*', onlyIfSuccessful: true)
          }catch(e){
            def stageName = 'release'
            if(currentBuild.currentResult == "FAILURE"){
              NotifyBuild(currentBuild.result, stageName)
            }
            currentBuild.result = "FAILURE"
            throw e
          }
        }
      }
      post{
        success {
          echo "R ${currentBuild.result} C ${currentBuild.currentResult}"
          script{
            def recipient_list = 'lukai@sunmi.com,xiaoxinwu@sunmi.com,yangshijie@sunmi.com,yangjibin@sunmi.com,gaofei@sunmi.com,lvsiwen@sunmi.com,ningrulin@sunmi.com,hanruifeng@sunmi.com'
            def details = """<p>请从以下URL下载： "<a href="${env.BUILD_URL}">${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>"</p>"""
            emailext(attachLog: false, body: details, mimeType: 'text/html', subject: 'Android Release Build 已加固完成', to: recipient_list)
          }
        } 
      }
    }
  }
}

def NotifyBuild(String buildStatus = 'STARTED', String stage){
  // build status of null means successful
  buildStatus =  buildStatus ?: 'SUCCESS'
 
  // Default values
  def colorName = 'RED'
  def colorCode = '#FF0000'
  def subject = "${buildStatus}: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'"
  def summary = "${subject} (${env.BUILD_URL})"
  def details = """<p>STARTED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p>
    <p>Check console output at "<a href="${env.BUILD_URL}/console">${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>"</p>"""
 
  // Override default values based on build status
  if (buildStatus == 'STARTED') {
    color = 'YELLOW'
    colorCode = '#FFFF00'
  } else if (buildStatus == 'SUCCESS') {
    color = 'GREEN'
    colorCode = '#00FF00'
  } else {
    color = 'RED'
    colorCode = '#FF0000'
  }

  def recipient_list = 'lukai@sunmi.com,xiaoxinwu@sunmi.com,yangshijie@sunmi.com,yangjibin@sunmi.com,ningrulin@sunmi.com'

  switch(stage){
    case 'build':
      emailext(attachLog: false, body: details, mimeType: 'text/html', subject: 'Android Release Branch 构建出错', to: recipient_list)
      break

    case 'deploy':
      emailext(attachLog: false, body: details, mimeType: 'text/html', subject: 'Android Release Branch 部署出错', to: recipient_list)
      break

    case 'test':
      emailext(attachLog: false, body: details, mimeType: 'text/html', subject: 'Android Release Branch 测试步骤出错', to: recipient_list)
      break

    case 'release':
      emailext(attachLog: false, body: details, mimeType: 'text/html', subject: 'Android Release Branch 加固步骤出错', to: recipient_list)
      break
  }
}