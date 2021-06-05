Releasing
=========

1. Change the version number in `app/build.gradle`
2. Update `release-notes/en-AU/default.txt` with the changes
3. `git tag -a vX.X.X -m "Release X.X.X"`
4. `git push --tags`
5. Test out the build in the alpha channel in [Google Play](https://play.google.com/apps/publish/).
