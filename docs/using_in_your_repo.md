# Using Hubitat CI in Your Own Repository

This guide shows you how to set up Hubitat CI testing in your own Hubitat app or device driver repository, including local development setup, environment variables, and GitHub Actions CI/CD.

## Table of Contents
- [Quick Start](#quick-start)
- [Project Structure](#project-structure)
- [Environment Variables](#environment-variables)
- [Setting Up GitHub Actions CI](#setting-up-github-actions-ci)
- [GitHub Codespaces Setup](#github-codespaces-setup)
- [Real-World Examples](#real-world-examples)

## Quick Start

### 1. Create Your Project Structure

A minimal Hubitat CI project needs these files:

```
your-hubitat-app/
├── .github/
│   └── workflows/
│       └── ci.yml           # GitHub Actions workflow
├── src/
│   └── test/
│       └── groovy/
│           └── YourAppTest.groovy
├── MyHubitatApp.groovy      # Your app or driver
├── build.gradle             # Gradle build configuration
├── settings.gradle          # Gradle settings
└── README.md
```

### 2. Configure build.gradle

Create a `build.gradle` file with these minimum contents:

```groovy
plugins {
    id 'groovy'
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/joelwetzel/hubitat_ci")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.token") ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    // Hubitat CI library
    implementation 'me.biocomp.hubitat_ci:hubitat_ci:0.49'
    
    // Spock testing framework
    testImplementation 'org.spockframework:spock-core:2.3-groovy-3.0'
    
    // Groovy
    implementation 'org.codehaus.groovy:groovy-all:3.0.9'
}

test {
    useJUnitPlatform()
}
```

**Note:** The version `0.49` may need to be updated to the latest version. Check the [latest release](https://github.com/joelwetzel/hubitat_ci/packages).

### 3. Create settings.gradle

Create a `settings.gradle` file:

```groovy
rootProject.name = 'your-hubitat-app'
```

### 4. Write Your First Test

Create a basic validation test in `src/test/groovy/YourAppTest.groovy`:

```groovy
import me.biocomp.hubitat_ci.app.HubitatAppSandbox
import spock.lang.Specification

class YourAppTest extends Specification {
    
    def "App passes basic validation"() {
        when:
            def sandbox = new HubitatAppSandbox(new File("MyHubitatApp.groovy"))
            sandbox.run()
        
        then:
            noExceptionThrown()
    }
}
```

## Environment Variables

Hubitat CI requires environment variables to authenticate with GitHub Packages where the library is hosted.

### Required Variables

- **`GITHUB_TOKEN`** - Personal access token or GitHub Actions token
- **`GITHUB_ACTOR`** - Your GitHub username
- **`GITHUB_REPOSITORY`** (optional) - Repository identifier (e.g., `username/repo`)

### Local Development Setup

#### Option 1: Export in Your Shell

Add these to your `~/.bashrc`, `~/.zshrc`, or equivalent:

```bash
export GITHUB_ACTOR="your-github-username"
export GITHUB_TOKEN="your-personal-access-token"
```

Then reload your shell:
```bash
source ~/.bashrc  # or ~/.zshrc
```

#### Option 2: Create a gradle.properties File

Create `~/.gradle/gradle.properties` (in your home directory):

```properties
gpr.user=your-github-username
gpr.token=your-personal-access-token
```

This keeps credentials out of your project directory and works across all Gradle projects.

#### Creating a GitHub Personal Access Token

1. Go to [GitHub Settings → Developer settings → Personal access tokens → Tokens (classic)](https://github.com/settings/tokens)
2. Click "Generate new token (classic)"
3. Give it a descriptive name like "Hubitat CI Local Development"
4. Select scopes:
   - ✅ `read:packages` (required to download the library)
5. Click "Generate token"
6. Copy the token immediately (you won't see it again)

**Security Note:** Keep your token secure. Never commit it to your repository.

### Verifying Environment Variables

Test that your environment is configured correctly:

```bash
# Check that variables are set
echo $GITHUB_ACTOR
echo $GITHUB_TOKEN

# Try building your project
./gradlew build
```

If you see authentication errors, double-check that:
1. Your token has the `read:packages` scope
2. The environment variables are exported in your current shell session
3. The token hasn't expired

## Setting Up GitHub Actions CI

GitHub Actions can automatically test your code on every push and pull request.

### Basic CI Workflow

Create `.github/workflows/ci.yml`:

```yaml
name: CI

on:
  push:
    branches: [ main, master ]
  pull_request:
    branches: [ main, master ]

jobs:
  test:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v4

    - name: Set up JDK 11
      uses: actions/setup-java@v4
      with:
        java-version: '11'
        distribution: 'temurin'

    - name: Grant execute permission for gradlew
      run: chmod +x gradlew

    - name: Run tests
      run: ./gradlew test
      env:
        GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        GITHUB_ACTOR: ${{ github.actor }}

    - name: Upload test results
      uses: actions/upload-artifact@v4
      if: always()
      with:
        name: test-results
        path: build/test-results/test/

    - name: Upload test reports
      uses: actions/upload-artifact@v4
      if: always()
      with:
        name: test-reports
        path: build/reports/tests/test/
```

### What This Workflow Does

1. **Triggers** on pushes and pull requests to main/master branches
2. **Sets up** JDK 11 environment
3. **Runs tests** using Gradle with automatic GitHub token
4. **Uploads** test results and reports as artifacts
5. **Works automatically** - GitHub provides `GITHUB_TOKEN` for you

### Advanced: Adding Test Reporting

For better test visibility, add the JUnit report action:

```yaml
    - name: Publish Test Report
      uses: mikepenz/action-junit-report@v4
      if: always()
      with:
        report_paths: '**/build/test-results/test/TEST-*.xml'
        check_name: Test Results
        detailed_summary: true
        include_passed: true
```

### Environment Variables in GitHub Actions

GitHub Actions **automatically provides** these variables:
- `${{ secrets.GITHUB_TOKEN }}` - Automatically created token with package access
- `${{ github.actor }}` - The username that triggered the workflow
- `${{ github.repository }}` - Full repository name (owner/repo)

You **don't need to create secrets** for these - they're built-in.

## GitHub Codespaces Setup

GitHub Codespaces provides cloud-based development environments. You can configure environment variables for automatic setup.

### Option 1: Codespaces Secrets (Recommended)

Set user-level secrets that work across all your codespaces:

1. Go to [GitHub Settings → Codespaces → Secrets](https://github.com/settings/codespaces)
2. Click "New secret"
3. Add secrets:
   - Name: `GITHUB_ACTOR`, Value: your-username
   - Name: `GITHUB_TOKEN`, Value: your-personal-access-token

These will be automatically available in all your codespaces.

### Option 2: Repository Codespaces Secrets

For repository-specific configuration:

1. Go to your repository → Settings → Secrets and variables → Codespaces
2. Add the same secrets as above

### Option 3: devcontainer.json Configuration

Create `.devcontainer/devcontainer.json`:

```json
{
  "name": "Hubitat Development",
  "image": "mcr.microsoft.com/devcontainers/java:11",
  "features": {
    "ghcr.io/devcontainers/features/java:1": {
      "version": "11"
    }
  },
  "remoteEnv": {
    "GITHUB_ACTOR": "${localEnv:GITHUB_ACTOR}",
    "GITHUB_TOKEN": "${localEnv:GITHUB_TOKEN}"
  },
  "postCreateCommand": "chmod +x gradlew && ./gradlew build"
}
```

This:
- Sets up Java 11
- Passes through your environment variables
- Automatically builds your project when the codespace starts

## Real-World Examples

Here are complete examples of repositories using Hubitat CI with different patterns:

### Example 1: Basic App with Integration Tests
**[Hubitat-Switch-Bindings](https://github.com/joelwetzel/Hubitat-Switch-Bindings)**

Project structure:
```
Hubitat-Switch-Bindings/
├── .github/workflows/ci.yml
├── Switch Bindings.groovy        # The app
├── src/test/groovy/
│   └── SwitchBindingsIntegrationTest.groovy
└── build.gradle
```

Key features:
- Integration tests using `IntegrationAppSpecification`
- Device fixtures for switches and dimmers
- Time-based testing with `TimeKeeper`
- GitHub Actions CI on every push

**Highlights from the tests:**
```groovy
class SwitchBindingsIntegrationTest extends IntegrationAppSpecification {
    def switch1
    def switch2
    
    def setup() {
        switch1 = SwitchFixtureFactory.create('Switch 1')
        switch2 = SwitchFixtureFactory.create('Switch 2')
        
        super.initializeEnvironment(
            appScriptFilename: "Switch Bindings.groovy",
            userSettingValues: [
                master: switch1,
                slaves: [switch2]
            ]
        )
        
        switch1.initialize(appExecutor, [switch: "off"])
        switch2.initialize(appExecutor, [switch: "off"])
        appScript.initialize()
    }
    
    def "Master switch controls slave switches"() {
        when: "Master turns on"
            switch1.on()
        
        then: "Slave follows"
            switch2.currentValue("switch") == "on"
    }
}
```

### Example 2: Complex Multi-Device App
**[Hubitat-Lockdown](https://github.com/joelwetzel/Hubitat-Lockdown)**

This app coordinates locks, switches, and modes across multiple devices.

Key features:
- Multiple device types (locks, switches, presence sensors)
- Mode management testing
- Scheduled behavior testing
- Complex state transitions

### Example 3: Minimal Example Project
**[hubitat_ci_example](https://github.com/biocomp/hubitat_ci_example)**

Contains multiple example patterns:
- Minimal app validation
- Device driver testing
- Mocking techniques
- Input/preference validation

Directory structure:
```
hubitat_ci_example/
├── minimal/                    # Simplest possible example
├── how_to_test/               # Various testing patterns
│   ├── app_script.groovy
│   ├── device_script.groovy
│   └── src/
│       ├── Mocking.groovy
│       └── ValidatingInputsAndProperties.groovy
└── build.gradle
```

### Example 4: Well-Documented Integration Tests
**[Hubitat-Auto-Shades](https://github.com/joelwetzel/Hubitat-Auto-Shades)**

Best practices example with:
- Detailed comments in test code
- Multiple test scenarios
- Window shade fixtures
- Time-of-day logic testing
- Comprehensive CI setup

## Testing Patterns

### Pattern 1: Validation Only (Minimal)

For basic structural validation without custom tests:

```groovy
class MyAppTest extends Specification {
    def "Validate app structure"() {
        expect:
            new HubitatAppSandbox(new File("MyApp.groovy")).run()
    }
}
```

### Pattern 2: Integration Testing (Recommended)

For testing actual behavior:

```groovy
class MyAppIntegrationTest extends IntegrationAppSpecification {
    def devices
    
    def setup() {
        devices = [
            SwitchFixtureFactory.create('Living Room'),
            SwitchFixtureFactory.create('Kitchen')
        ]
        
        super.initializeEnvironment(
            appScriptFilename: "MyApp.groovy",
            userSettingValues: [switches: devices]
        )
        
        devices.each { it.initialize(appExecutor, [switch: "off"]) }
        appScript.initialize()
    }
    
    def "Test your behavior here"() {
        when:
            devices[0].on()
        
        then:
            1 * log.debug(_)
    }
}
```

### Pattern 3: Mixed Testing

Combine validation and integration tests in the same project:

```
src/test/groovy/
├── validation/
│   └── StructureValidationTest.groovy
└── integration/
    ├── DeviceInteractionTest.groovy
    └── SchedulingTest.groovy
```

## Troubleshooting

### "Could not resolve dependency" Error

**Problem:** Gradle can't download hubitat_ci library.

**Solutions:**
1. Verify environment variables are set: `echo $GITHUB_TOKEN`
2. Check your personal access token has `read:packages` scope
3. Verify the token hasn't expired
4. Check you're using the correct repository URL in build.gradle

### Tests Run Locally But Fail in CI

**Problem:** Tests pass on your machine but fail in GitHub Actions.

**Common causes:**
1. Different time zones (use `TimeKeeper` for all time-based logic)
2. Missing files (ensure all files are committed to git)
3. Path issues (use relative paths from project root)

### Gradle Wrapper Not Found

**Problem:** `./gradlew: Permission denied` or `gradlew not found`

**Solutions:**
```bash
# Initialize Gradle wrapper
gradle wrapper

# Make it executable
chmod +x gradlew

# Commit the wrapper files
git add gradlew gradlew.bat gradle/
git commit -m "Add Gradle wrapper"
```

## Next Steps

1. **Review the examples**: Look at the real-world repositories linked above
2. **Start simple**: Begin with validation tests, then add integration tests
3. **Read the guides**:
   - [Integration Testing Guide](integration_testing.md) - Detailed integration testing
   - [How to Test](how_to_test.md) - Mocking and validation techniques
4. **Set up CI early**: Get GitHub Actions running from the start
5. **Iterate**: Add more tests as you develop new features

## Additional Resources

- [Spock Framework Documentation](http://spockframework.org/) - Testing framework used by Hubitat CI
- [Gradle User Guide](https://docs.gradle.org/current/userguide/userguide.html) - Build tool documentation
- [GitHub Actions Documentation](https://docs.github.com/en/actions) - CI/CD platform
- [Hubitat Documentation](https://docs2.hubitat.com/) - Hubitat platform reference
