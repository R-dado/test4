# spacewalk
The Gemini protocol feature-rich* browser for Android.

<sup>* - not yet</sup>

## Can-s
- Doing Gemini protocol stuff completely ignoring certificate stuff
  - Sending requests and getting responses via SSL
  - Handling errors (just displaying their status codes)
  - Handling input
    - Showing status-code `11` input as sensitive (like password dots)
  - Navigating through links and bottom app bar
- Gemtext parsing and displaying
  - Displaying `pre` tags in monospace and with horizontal scrolling
- Rendering any other text as plain text
- Navigating back in history
- Homepage (hardcoded `https://gemini.circumlunar.space/`)
- Opening `gemini://` links from other apps
- Opening not `gemini://` links in other apps
- Showing links URI on long-click

## Can't-s
- Loading any data that is not `text/*`
- Multilevel lists
- Navigating forward in history
- Tabs
- Any customization inside the app (though code is written with customization in priority)
- Any certificate-related things
- Settings, preferences, options and other similar terms
- Anything else

## Planned
- Homepage customization
- Tabs
- Fully working history navigation
- Favorites
- Favicon loading
- Customizing any line type in user-friendly way
  - Text size
  - Fonts
  - Colors in light/dark modes
  - Bold and/or Italic style
  - Line spacing
  - Padding sizes
- Security settings
  - Server certificate management
  - Client certificate management

## Seems interesting to be added
- Displaying popular images formats
- Parsing Markdown (but without sideload stuff like images)
- Parsing Gopher markup
