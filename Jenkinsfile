pipeline {
    agent any
    
    tools {
        // Debe configurarse en Jenkins la instalación de JDK y Maven
        jdk 'JDK11'  // O la versión que uses en tu proyecto
        maven 'Maven3'  // La versión disponible en tu Jenkins
    }
    
    stages {
        stage('Checkout') {
            steps {
                // Este paso se realiza automáticamente en most configuraciones
                checkout scm
            }
        }
        
        stage('Compilación') {
            steps {
                sh 'mvn clean compile'
            }
        }
        
        stage('Pruebas Unitarias') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    // Recoger resultados de pruebas unitarias
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Cobertura de Código') {
            steps {
                // Asume que JaCoCo está configurado en el pom.xml
                sh 'mvn jacoco:report'
            }
            post {
                success {
                    // Publicar reportes de cobertura
                    jacoco(
                        execPattern: '**/target/*.exec',
                        classPattern: '**/target/classes',
                        sourcePattern: '**/src/main/java'
                    )
                }
            }
        }
        
        stage('Pruebas de Integración') {
            steps {
                // Usando el plugin failsafe para pruebas de integración
                sh 'mvn failsafe:integration-test'
            }
            post {
                always {
                    // Recoger resultados de pruebas de integración
                    junit '**/target/failsafe-reports/*.xml'
                }
            }
        }
        
        stage('Pruebas de Sistema') {
            steps {
                // Asume que tienes un perfil de Maven específico para pruebas de sistema
                // Podría incluir TestContainers para probar con base de datos PostgreSQL
                sh 'mvn -P system-tests verify'
            }
            post {
                always {
                    // Recoger resultados de pruebas de sistema
                    junit '**/target/system-reports/*.xml'
                }
            }
        }
        
        stage('Construir Artefacto') {
            steps {
                sh 'mvn package -DskipTests'
            }
            post {
                success {
                    // Archivar el archivo WAR o JAR generado
                    archiveArtifacts artifacts: '**/target/*.war', fingerprint: true
                }
            }
        }
    }
    
    post {
        always {
            // Limpiar el workspace después de la ejecución
            cleanWs()
        }
        success {
            echo 'Pipeline ejecutado correctamente'
        }
        failure {
            echo 'El pipeline ha fallado'
        }
    }
}
