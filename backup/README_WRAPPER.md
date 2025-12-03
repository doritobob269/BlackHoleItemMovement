Gradle wrapper helper

Files added:
- `gradlew` (Unix shell)
- `gradlew.bat` (Windows PowerShell / CMD)
- `gradle/wrapper/gradle-wrapper.properties`

Notes:
- The `gradle-wrapper.jar` binary is not included here. If you already have Gradle installed you can generate the jar and other wrapper files by running:

  ```powershell
  gradle wrapper
  ```

  or for a specific Gradle version:

  ```powershell
  gradle wrapper --gradle-version 8.4.1
  ```

- After the wrapper JAR exists, run the dev client with:

  ```powershell
  .\gradlew.bat --no-daemon runClient
  ```

- If you don't have Gradle installed and want me to add the `gradle-wrapper.jar` file into the repo, tell me and I'll add it (note: it is a ~MB binary file).
