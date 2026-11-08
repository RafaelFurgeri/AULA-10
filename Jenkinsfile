// ============================================================================
// Aula 10 - Atividade 2: Pipeline CD com Jenkins
// Inclui:
// - Build com Maven
// - Analise estatica Checkstyle + PMD (Exercicio 1 da Atividade 2)
// - Testes Unitarios e Integracao (TestContainers) em paralelo + JaCoCo (> 85%)
// - Seguranca com OWASP Dependency-Check
// - Approval manual antes do deploy
// - Deploy em Staging com Rollback automatico em caso de falha (Exercicio 2)
// - Notificacao Slack em caso de falha
// ============================================================================

pipeline {
    agent any

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {
        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests -B'
            }
        }

        // Exercicio 1 (Atividade 2): Estagio dedicado para analise estatica PMD e Checkstyle
        stage('Static Analysis (PMD & Checkstyle)') {
            steps {
                sh 'mvn checkstyle:check pmd:pmd pmd:check -B'
            }
            post {
                always {
                    archiveArtifacts artifacts: 'target/pmd.xml, target/checkstyle-result.xml', allowEmptyArchive: true
                }
            }
        }

        stage('Automated Tests & Coverage') {
            parallel {
                stage('Unit Tests & JaCoCo (> 85%)') {
                    steps {
                        sh 'mvn test jacoco:report jacoco:check -B'
                    }
                    post {
                        always {
                            junit 'target/surefire-reports/**/*.xml'
                            archiveArtifacts artifacts: 'target/site/jacoco/**/*', allowEmptyArchive: true
                        }
                    }
                }

                stage('Integration Tests (TestContainers)') {
                    steps {
                        sh 'mvn verify -Pintegration-tests -DskipUnitTests=true -Djacoco.minimum=0.00 -B'
                    }
                    post {
                        always {
                            junit allowEmptyResults: true, testResults: 'target/failsafe-reports/**/*.xml'
                        }
                    }
                }
            }
        }

        stage('Security Scan (OWASP Dependency-Check)') {
            steps {
                sh 'mvn dependency-check:check -B'
            }
            post {
                always {
                    archiveArtifacts artifacts: 'target/dependency-check-report.*', allowEmptyArchive: true
                }
            }
        }

        stage('Manual Approval') {
            when {
                branch 'main'
            }
            steps {
                input message: 'Todos os testes e Quality Gates passaram. Aprovar deploy para Staging?',
                      ok: 'Aprovar Deploy'
            }
        }

        stage('Deploy to Staging') {
            when {
                branch 'main'
            }
            steps {
                sh 'docker build -t myapp:${GIT_COMMIT} .'
                sh 'kubectl apply -f k8s/deployment.yaml'
                sh 'kubectl apply -f k8s/service.yaml'
                sh 'kubectl set image deployment/myapp-deployment myapp=myapp:${GIT_COMMIT}'
                sh 'kubectl rollout status deployment/myapp-deployment --timeout=120s'
            }
            // Exercicio 2 (Atividade 2): Rollback automatico em caso de falha no deploy
            post {
                failure {
                    echo 'ERRO no deploy! Iniciando rollback automatico para a revisao estavel anterior...'
                    sh 'kubectl rollout undo deployment/myapp-deployment'
                    sh 'kubectl rollout status deployment/myapp-deployment --timeout=60s'
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline concluida com sucesso: ${env.JOB_NAME} #${env.BUILD_NUMBER}"
        }
        failure {
            slackSend channel: '#alerts', message: "Build Failed: ${env.JOB_NAME} #${env.BUILD_NUMBER} (${env.BUILD_URL})"
        }
    }
}
