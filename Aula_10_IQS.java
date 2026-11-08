name: Java CI Pipeline  # Nome da pipeline
on: [push, pull_request]  # Dispara em pushes e pull requests

jobs:
  build-and-test:
    runs-on: ubuntu-latest  # Executa em container Ubuntu
    steps:
      - uses: actions/checkout@v3  # Faz checkout do código
      
      # Configura ambiente Java
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'  # Usa JDK 17
          distribution: 'temurin'  # Distribuição Eclipse Temurin
      
      # Etapa de build
      - name: Build with Maven
        run: mvn clean package  # Limpa e gera o pacote
      
      # Etapa de testes unitários
      - name: Run Unit Tests
        run: mvn test  # Executa testes unitários
      
      # Verificação de qualidade de código
      - name: Code Quality Check
        run: mvn checkstyle:check  # Valida estilo de código
        continue-on-error: false  # Falha o build se encontrar erros
		
_____________________________________________________

      # Análise estática com SonarQube
      - name: SonarQube Scan
        uses: SonarSource/sonarqube-scan-action@master
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}  # Token de autenticação
          SONAR_HOST_URL: ${{ secrets.SONAR_HOST_URL }}  # URL do SonarQube
		  
_____________________________________________________

pipeline {
    agent any  # Executa em qualquer agente disponível
    stages {
        // Etapa de construção
        stage('Build') {
            steps {
                sh 'mvn clean package'  # Comando Maven
            }
        }
        
        // Etapa de testes
        stage('Test') {
            steps {
                sh 'mvn test'  # Executa testes
                junit 'target/surefire-reports/**/*.xml'  # Publica relatórios
            }
        }
        
        // Etapa condicional de deploy
        stage('Deploy to Staging') {
            when {
                branch 'main'  # Só executa no branch main
            }
            steps {
                // Build da imagem Docker
                sh 'docker build -t myapp:${GIT_COMMIT} .'
                // Deploy no Kubernetes
                sh 'kubectl apply -f k8s/deployment.yaml'
            }
        }
    }
    
    // Ações pós-build
    post {
        failure {
            // Notificação no Slack em caso de falha
            slackSend channel: '#alerts', message: "Build Failed: ${env.JOB_NAME}"
        }
    }
}

_____________________________________________________

# Pipeline específica para Quality Gate
name: Quality Gate
on: pull_request  # Dispara em PRs

jobs:
  sonarqube:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: SonarQube Scan
        uses: SonarSource/sonarqube-scan-action@master
        with:
          args: >
            -Dsonar.qualitygate.wait=true  # Espera pela análise de Quality Gate
			
_____________________________________________________

jobs:
  unit-tests:
    # ... (configuração de testes unitários)
    
  # Testes de integração com dependência
  integration-tests:
    needs: build  # Espera o job de build
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - run: mvn verify -Pintegration-tests  # Executa testes de integração
	  