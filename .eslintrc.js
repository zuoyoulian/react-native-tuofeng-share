module.exports = {
  "rules": {
    "react/jsx-filename-extension": [1, { "extensions": [".js", ".jsx"] }],
    "react/jsx-uses-react": "error",
    "react/jsx-uses-vars": "error",
    "react/no-unescaped-entities": "off",
    "import/no-unresolved": [2, { ignore: ['\.webp$', 'Toast$', 'RichText$', 'weixin$', 'GoogleAnalytics$', 'JijiaPush$', 'ZoomImage$', 'DatePicker$', 'Audio$', 'Recorder$', 'Swiper$', 'SegmentedControl$', 'react-native-audio-android', 'SwipeableListView', 'SwipeableQuickActions', 'react-native-webview-android']}],
    'import/extensions': ['off', 'never'],
    "no-underscore-dangle": ['error', {allowAfterThis: true}],
    "max-len": [1, 300],
    "indent": [
      2,
      2
    ],
    "quotes": [
      2,
      "single"
    ],
    "linebreak-style": [
      2,
      "unix"
    ],
    "semi": [
      2,
      "never"
    ]
  },
  "env": {
    "es6": true,
    "node": true,
    "browser": true
  },
  "parser": "babel-eslint",
  "extends": ["eslint:recommended", "plugin:react/recommended", "airbnb"],
  "ecmaFeatures": {
    "modules": true,
    "jsx": true,
    "experimentalObjectRestSpread": true
  },
  "parserOptions": {
    "ecmaFeatures": {
      "jsx": true
    }
  },
  "plugins": [
    "react",
    "react-native"
  ]
};
