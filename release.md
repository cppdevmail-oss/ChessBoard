# How to Publish an App on Google Play

## 1. Prepare the release build

- Set the final `applicationId`
- Increase `versionCode`
- Set `versionName`
- Use the required `targetSdk`

## 2. Create a signed Android App Bundle

In Android Studio:

```text
Build → Generate Signed Bundle / APK → Android App Bundle
```

Output file:

```text
.aab
```

## 3. Create a Google Play Developer account

- Register in Google Play Console
- Complete account verification
- Pay the developer fee

## 4. Create the app in Play Console

- Choose app name
- Select default language
- Select app or game
- Choose free or paid

## 5. Fill the store listing

- App description
- App icon
- Screenshots
- Feature graphic
- Category
- Contact details

## 6. Fill the App Content section

- Privacy Policy
- Data Safety
- Ads declaration
- Content rating
- Target audience
- App access details

## 7. Upload the `.aab`

Go to:

```text
Testing → Internal testing
```

Upload the signed `.aab`.

## 8. Test the app

- Add testers
- Install the app from the Google Play test link
- Check that everything works

## 9. Release to production

Go to:

```text
Production → Create new release
```

Then:

- Upload the `.aab`
- Review the release
- Submit it for review

## 10. Wait for Google review

After approval, the app becomes available on Google Play.
