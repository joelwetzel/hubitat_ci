# Publishing to Maven

This guide explains how to publish an updated version of the library to Maven Central (Azure Artifacts).

## Prerequisites

To publish to Maven, you need:

1. **Azure DevOps Access**: Permission to trigger the deployment pipeline
2. **Azure Artifacts Credentials**: The `AZURE_ARTIFACTS_ENV_ACCESS_TOKEN` environment variable or `azureArtifactsGradleAccessToken` gradle property configured

The Azure Pipelines CI automatically handles publishing when triggered through the deployment pipeline.

## Publishing Process

### 1. Update the Version Number

Edit [build.gradle](../build.gradle) and update the version number in the publishing configuration:

```groovy
publishing {
    publications {
        maven(MavenPublication){
            groupId = 'me.biocomp.hubitat_ci'
            artifactId = System.getenv("MAVEN_ARTIFACT_ID") ?: 'hubitat_ci'
            version = 0.45  // Update this version number

            from components.java

            artifact sourcesJar
            artifact groovydoc
        }
    }
    // ...
}
```

**Version Numbering Guidelines:**
- Increment the minor version (0.45 → 0.46) for backward-compatible changes
- Increment the major version (0.x → 1.0) for breaking changes
- Consider using semantic versioning (MAJOR.MINOR.PATCH) for clarity

### 2. Test Your Changes

Before publishing, ensure all tests pass:

```bash
# Run all tests
./gradlew test

# Run full verification (tests + checks)
./gradlew check
```

You can also run continuous testing during development:

```bash
./gradlew test --continuous
```

### 3. Commit and Push Your Changes

```bash
git add build.gradle
git commit -m "Bump version to 0.XX"
git push origin master
```

The [azure-pipelines.yml](../azure-pipelines.yml) will automatically build and test your changes.

### 4. Publish via Azure DevOps

Publishing is done through a manual deployment pipeline:

1. Navigate to the Azure DevOps project
2. Go to Pipelines → Select the **deploy.yml** pipeline
3. Click "Run pipeline"
4. Confirm and trigger the deployment

The deployment pipeline ([deploy.yml](../deploy.yml)) will:
- Check out the code with submodules
- Build the project
- Generate Groovydoc documentation
- Publish artifacts to Maven (Azure Artifacts)

### 5. Verify the Publication

After publishing completes:

1. Check the Azure Artifacts feed at:
   ```
   https://biocomp.pkgs.visualstudio.com/HubitatCiRelease/_packaging/hubitat_ci_feed/maven/v1
   ```

2. Verify the new version is available

3. Test consuming the new version in a dependent project:
   ```groovy
   dependencies {
       implementation 'me.biocomp.hubitat_ci:hubitat_ci:0.XX'
   }
   ```

## Local Publishing (For Testing)

To test the publishing process locally without actually publishing:

```bash
# Build everything including sources and docs
./gradlew build sourcesJar groovydocJar

# Check what would be published
./gradlew publish --dry-run
```

To publish to your local Maven repository for testing:

```bash
# Add to build.gradle temporarily:
repositories {
    mavenLocal()
}

# Publish locally
./gradlew publishToMavenLocal
```

## Troubleshooting

### Authentication Errors

If you see authentication errors, ensure:
- You have valid Azure Artifacts credentials
- The `AZURE_ARTIFACTS_ENV_ACCESS_TOKEN` environment variable is set, or
- The `azureArtifactsGradleAccessToken` is configured in `~/.gradle/gradle.properties`

### Build Failures

If the build fails during publishing:
1. Check that all tests pass locally first
2. Review the Azure Pipeline logs for specific errors
3. Ensure the version number hasn't already been published

### Version Conflicts

If the version already exists:
- Azure Artifacts may reject the publication
- Increment the version number and try again
- Consider whether you need to delete the old version (use with caution)

## Publishing Checklist

Before publishing a new version:

- [ ] Update version number in [build.gradle](../build.gradle)
- [ ] Run `./gradlew test` - all tests pass
- [ ] Update [changelog.md](changelog.md) with release notes
- [ ] Commit and push changes
- [ ] Wait for CI build to complete successfully
- [ ] Trigger deployment pipeline in Azure DevOps
- [ ] Verify published artifacts in Azure Artifacts feed
- [ ] Tag the release in Git: `git tag v0.XX && git push --tags`
- [ ] Update dependent projects to use new version

## Related Documentation

- [Getting Started](getting_started.md) - Initial setup for users
- [Contributing Guide](../contributing.md) - Guidelines for contributors
- [Changelog](changelog.md) - Version history and release notes
