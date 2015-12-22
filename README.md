Frontend Email Reporter
=================================

This application provides dashboards for email product from theguardian.com

### Install client-side dependencies

```
npm install
npm run devSetup
```

A good way to check everything is setup correctly is to run the tests

```
npm test
```

For development you'll also need the following commands:

**Compile assets**

```
npm run compile
```

**Watch files for changes**

```
npm run watch
```

*Note:* We use `grunt` and `bower` behind the scenes but provide [facades for common tasks](https://bocoup.com/weblog/a-facade-for-tooling-with-npm-scripts/) to make setup easier and reduce the number of tools needed for most developers. If you want to work with the full set of build tools install `grunt-cli` and run `grunt --help` to see the list of available tasks.

To generate cloudformation run:
```
npm run cloudformation --loglevel silent  > cloudformation.json
```
Then upload via the AWS console