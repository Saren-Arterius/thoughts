# Thoughts
Inspired by "Secret" pages among Hong Kong, Thoughts is an application that allows you to speak your little secret thought anonymously. A CCN2279 Group project.

# Download
https://play.google.com/store/apps/details?id=net.wtako.thoughts&hl=zh_HK

# Functions
- No login required
- Voting
- Favourite / Bookmark / Reading List
- Hashtag
- Sorting
- Search
- Home screen widget
- GCM

# Self hosting Thoughts REST backend?
Edit `thoughts/app/src/main/java/net/wtako/thoughts/Thoughts.java`, change the value of `public static final String AUTHORITY` to your backend, for example, `http://192.168.0.100:3000`, then rebuild the APK for yourself.

### Making GCM work
1. Rename the package
2. Enable Google service & GCM for your package
3. Generate `google-services.json` and place it under `thoughts/app`
4. Be sure to find out the API key for the [backend server](https://github.com/Saren-Arterius/thoughts-rest)

# FOSS
Thoughts, including its backend server, is also a FOSS application.
- Android app source code (Apache-2.0): https://github.com/Saren-Arterius/thoughts
- Backend server source code (Apache-2.0): https://github.com/Saren-Arterius/thoughts-rest
