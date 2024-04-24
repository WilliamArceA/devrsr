pipeline{
    agent{
        label 'jenkins_slave' 
    }
    tools{
        nodejs 'node20'
    }
    environment {
        workspace="/data/"
    }
    stages{
        stage("Limpiar"){
            steps{
                cleanWs()
            }
        }
        stage("Download project"){
            steps{
                git credentialsId: 'git_credentials', branch: "develop", url: "https://github.com/WilliamArceA/Api-Activos.git"
                echo "Proyecto descargado"
            }
        }
        stage("Build project"){
            steps{
                echo "Iniciando build"
                sh "npm version"
                sh "pwd"
                sh "npm install"
                sh "pwd"
                sh "tar -czf project_files.tar.gz node_modules *.json"
                stash includes: 'project_files.tar.gz', name: 'backartifact'
                archiveArtifacts artifacts: 'project_files.tar.gz', onlyIfSuccessful:true
                sh "cp project_files.tar.gz /tmp/"
            }
        }
        stage("Vulnerability test"){
            steps{
                sh "/grype /tmp/project_files.tar.gz > informe-scan.txt"
                sh "pwd"
                archiveArtifacts artifacts: 'informe-scan.txt', onlyIfSuccessful:true
            }
        }
        /*stage("Sonarqube analysis"){
            steps{
                script{
                    sh "pwd"
                    sh "ls"
                        writeFile encoding: 'UTF-8', file: 'sonar-project.properties', text: """sonar.projectKey=actives
                        sonar.projectName=actives
                        sonar.projectVersion=1.0.0
                        sonar.sourceEncoding=UTF-8
                        sonar.sources=./src
                        sonar.javascript.libraries=./node_modules
                        sonar.language=javascript
                        sonar.scm.provider=git
                        """
                        withSonarQubeEnv('Sonar_CI'){
                            def scannerHome = tool 'Sonar_CI'
                            sh "${tool("Sonar_CI")}/bin/sonar-scanner -X"
                        }
                }
            }
        }*/
    }
}