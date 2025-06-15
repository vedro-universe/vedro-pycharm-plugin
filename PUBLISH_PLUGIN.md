# Publishing Vedro Plugin

A quick reference for releasing a new version of the Vedro PyCharm plugin.

## Steps

1. **Update version**

   * Open `src/main/resources/META-INF/plugin.xml`
   * Change the `<version>` element to the new release number.

2. **Build the plugin**

   ```bash
   ./gradlew buildPlugin
   ```

3. **Upload to JetBrains Marketplace**

   1. Go to: [https://plugins.jetbrains.com/plugin/18227-vedro/edit](https://plugins.jetbrains.com/plugin/18227-vedro/edit)
   2. Click **Upload Update**
   3. Select the ZIP from `build/distributions/Vedro.zip`
