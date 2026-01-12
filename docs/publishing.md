# Publishing to Maven

This guide explains how to publish an updated version of the library to GitHub Packages (Maven).

## Prerequisites

To publish to Maven locally, you need:

1. **GitHub Personal Access Token**: With `write:packages` and `read:packages` scopes
2. **Environment Variables**: `GITHUB_TOKEN`, `GITHUB_ACTOR`, and `GITHUB_REPOSITORY` configured

For automatic publishing via CI/CD, no setup is required - GitHub Actions handles it automatically.

## Publishing Process

### 1. Update the Version Number

Edit [build.gradle](../build.gradle) and update the version number in the publishing configuration:

```groovy
publishing {
    publications {
        maven(MavenPublication) {
            groupId = 'me.biocomp.hubitat_ci'
            artifactId = 'hubitat_ci'
            version = 0.48  // Update this version number

            from components.java

            artifact sourcesJar
            artifact groovydocJar
        }
    }
    // ...
}
```

**Version Numbering Guidelines:**
- Increment the minor version (0.48 → 0.49) for backward-compatible changes
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

The [GitHub Actions workflow](.github/workflows/ci.yml) will automatically:
- Run all tests
- Check if the version already exists in GitHub Packages
- Publish the new version if it doesn't exist
- Skip publishing if the version already exists (with a warning message)

### 4. Verify Automatic Publication

After pushing to master:

1. Go to the **Actions** tab in your GitHub repository
2. Watch the CI/CD Pipeline workflow run
3. The workflow will:
   - Run tests
   - Check if version already exists
   - Publish automatically if it's a new version
   - Skip publishing if the version already exists

### 5. Verify the Publication

After publishing completes:

1. Check GitHub Packages on your repository page:
   - Click on "Packages" on the right sidebar
   - Find `me.biocomp.hubitat_ci.hubitat_ci`
   - Verify the new version is listed

2. Test consuming the new version in a dependent project:
   ```groovy
   repositories {
       maven {
           url = uri("https://maven.pkg.github.com/joelwetzel/hubitat_ci")
           credentials {
               username = project.findProperty("gpr.user") ?: System.getenv("GITHUB_ACTOR")
               password = project.findProperty("gpr.token") ?: System.getenv("GITHUB_TOKEN")
           }
       }
   }

   dependencies {
       implementation 'me.biocomp.hubitat_ci:hubitat_ci:0.XX'
   }
   ```

## Local Publishing (For Testing)

To publish locally for testing a new version before pushing to master:

### Setup Local Environment

First, create a Personal Access Token:
1. Go to GitHub Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Generate new token with `write:packages` and `read:packages` scopes
3. Set environment variables:

```bash
export GITHUB_TOKEN=your_personal_access_token_here
export GITHUB_ACTOR=your_github_username
export GITHUB_REPOSITORY=joelwetzel/hubitat_ci
```

### Publish to GitHub Packages

```bash
# Build everything including sources and docs
./gradlew build sourcesJar groovydocJar

# Publish to GitHub Packages
./gradlew publish
```

### Publish to Local Maven (Alternative)

To publish to your local Maven repository for testing without GitHub:

```bash
# Publish locally
./gradlew publishToMavenLocal
```

This installs to `~/.m2/repository/` for local testing.

## Troubleshooting

### Authentication Errors

If you see authentication errors when publishing locally:
- Ensure your Personal Access Token has `write:packages` scope
- Verify `GITHUB_TOKEN`, `GITHUB_ACTOR`, and `GITHUB_REPOSITORY` environment variables are set
- Token format should be `github_pat_...` (classic token)

### Build Failures

If the build fails during publishing:
1. Check that all tests pass locally first: `./gradlew test`
2. Review the GitHub Actions logs in the Actions tab
3. Ensure the version number hasn't already been published

### Version Already Exists

If publishing fails with HTTP 409 Conflict:
- The version already exists in GitHub Packages
- Update the version number in [build.gradle](../build.gradle)
- The CI/CD pipeline automatically detects this and skips publishing with a warning

### Submodule Issues in CI

If tests fail due to missing submodules:
- The workflow should have `submodules: true` in checkout steps
- Check [.github/workflows/ci.yml](.github/workflows/ci.yml) for proper configuration

## Publishing Checklist

Before publishing a new version:

- [ ] Update version number in [build.gradle](../build.gradle)
- [ ] Run `./gradlew test` - all tests pass
- [ ] Update [changelog.md](changelog.md) with release notes
- [ ] Commit and push changes to master
- [ ] Wait for GitHub Actions workflow to complete
- [ ] Verify CI tests passed
- [ ] Verify package published (or skipped if version exists)
- [ ] Check GitHub Packages for the new version
- [ ] Tag the release in Git: `git tag v0.XX && git push --tags`
- [ ] Update dependent projects to use new version

## Related Documentation

- [Getting Started](getting_started.md) - Initial setup for users
- [Contributing Guide](../contributing.md) - Guidelines for contributors
- [Changelog](changelog.md) - Version history and release notes
