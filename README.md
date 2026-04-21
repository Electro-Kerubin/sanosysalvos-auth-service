# sanosysalvos-auth-service

## SonarQube en GitHub Actions

Se agrego el workflow `/.github/workflows/sonarqube.yml` para ejecutar build, tests y analisis SonarQube en `push`/`pull_request` de `main`.

Configura estos secretos en GitHub (`Settings > Secrets and variables > Actions`):

- `SONAR_TOKEN`: token de usuario de SonarQube.
- `SONAR_HOST_URL`: URL del servidor SonarQube (ejemplo: `https://sonar.tudominio.cl`).
- `SONAR_PROJECT_KEY`: clave unica del proyecto en SonarQube.

Comando local equivalente:

```bash
mvn -B verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
  -Dsonar.projectKey=<TU_PROJECT_KEY> \
  -Dsonar.host.url=<TU_SONAR_URL> \
  -Dsonar.token=<TU_SONAR_TOKEN>
```
