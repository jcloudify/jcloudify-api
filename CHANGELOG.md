## [3.15.1](https://github.com/jcloudify/jcloudify-api/compare/v3.15.0...v3.15.1) (2024-08-21)


### Bug Fixes

* stack appname is not anymore given as parameter ([ae80005](https://github.com/jcloudify/jcloudify-api/commit/ae8000598c81468962fddb44a608d87284aca31c))
* **tests:** correctly map nullable values for poja conf ([f37f583](https://github.com/jcloudify/jcloudify-api/commit/f37f583f2127efd03a692da70a8c69b1c5c0e539))



# [3.15.0](https://github.com/jcloudify/jcloudify-api/compare/v3.14.0...v3.15.0) (2024-08-20)


### Features

* deploy needed stacks before compute stack ([a723796](https://github.com/jcloudify/jcloudify-api/commit/a723796d7d6c7cf469fe11eff19d1d45a3f0f31f))
* **no-implementation:** make some poja conf nullable ([cb0fbf0](https://github.com/jcloudify/jcloudify-api/commit/cb0fbf0e87ec4f34f080528d3d23640c28beceb8))



# [3.14.0](https://github.com/jcloudify/jcloudify-api/compare/v3.13.0...v3.14.0) (2024-08-14)


### Bug Fixes

* rename build_template_file_url to build_template_file_uri ([e9f44cc](https://github.com/jcloudify/jcloudify-api/commit/e9f44cc98b46c10774d72dfa36a27b34869ca25d))
* retrieve payment details ([5682086](https://github.com/jcloudify/jcloudify-api/commit/5682086cc0e1ceef5938ae7558c525ce8cddf749))


### Features

* handle s3 putobject presigned url ([197623d](https://github.com/jcloudify/jcloudify-api/commit/197623dd36e45b3bdbcd5b01f6758a44794f44cd))



# [3.13.0](https://github.com/jcloudify/jcloudify-api/compare/v3.12.0...v3.13.0) (2024-08-08)


### Bug Fixes

* detach payment method ([ce763b9](https://github.com/jcloudify/jcloudify-api/commit/ce763b9e75e25179f0f0ecff1b4447245fca7d52))


### Features

* payment customer ([9e782ac](https://github.com/jcloudify/jcloudify-api/commit/9e782acaaf27018fbfc89fa73e20cd0a54e56dd5))



# [3.12.0](https://github.com/jcloudify/jcloudify-api/compare/v3.11.0...v3.12.0) (2024-08-06)


### Bug Fixes

* wrong exception caught during authentication ([ae73a60](https://github.com/jcloudify/jcloudify-api/commit/ae73a60a4830754d1de3a5321928d15077b39b29))


### Features

* authenticate either with github user token or app token ([e25dc17](https://github.com/jcloudify/jcloudify-api/commit/e25dc17c170a490e08c87da662491409ae39b413))
* refresh github access token ([13b0337](https://github.com/jcloudify/jcloudify-api/commit/13b033751fb952170f00cab906673f6647418469))
* upload project package file ([75e661f](https://github.com/jcloudify/jcloudify-api/commit/75e661f83fd2934bf4dc608d1fe32fb2e805749d))



# [3.11.0](https://github.com/jcloudify/jcloudify-api/compare/v3.10.1...v3.11.0) (2024-08-01)


### Features

* return refresh token with access token ([3caf931](https://github.com/jcloudify/jcloudify-api/commit/3caf931588e57eeafd6b736cb4bc0227b15a8302))



## [3.10.1](https://github.com/jcloudify/jcloudify-api/compare/v3.10.0...v3.10.1) (2024-07-31)


### Bug Fixes

* pojaConf snapstart is boolean ([1d55fed](https://github.com/jcloudify/jcloudify-api/commit/1d55fedcb937637f0e70b9f775769c8c7b94c419))
* return an empty list when there's no ssm param yet ([e181a41](https://github.com/jcloudify/jcloudify-api/commit/e181a413c0550787458a39754015a6dc67aefda3))
* separate endpoint to create and update ssm parameters ([2798bb0](https://github.com/jcloudify/jcloudify-api/commit/2798bb0641ff89d389d426ecd45a311df2b68aae))



# [3.10.0](https://github.com/jcloudify/jcloudify-api/compare/v3.9.0...v3.10.0) (2024-07-30)


### Features

* add avatar url and type to app installation, also rename app installation to a more precise GithubAppInstallation ([c1d4ab3](https://github.com/jcloudify/jcloudify-api/commit/c1d4ab34ca2fdd83209e1cde196ed2494bd8bbbc))



# [3.9.0](https://github.com/jcloudify/jcloudify-api/compare/v3.8.0...v3.9.0) (2024-07-30)


### Features

* crupdate ssm parameters ([de4ff05](https://github.com/jcloudify/jcloudify-api/commit/de4ff0529dddac330b691488c64c23678c13e947))
* get ssm parameters ([514a589](https://github.com/jcloudify/jcloudify-api/commit/514a589b2b9143356dd6e8d95277906c7fb7e024))
* manage payment method ([3ddba4b](https://github.com/jcloudify/jcloudify-api/commit/3ddba4b617217d6966016c10116b8ee50d6ede64))



# [3.8.0](https://github.com/jcloudify/jcloudify-api/compare/v3.7.0...v3.8.0) (2024-07-29)


### Bug Fixes

* only one prod and preprod env by application can be created ([925224e](https://github.com/jcloudify/jcloudify-api/commit/925224e49584c08842f55d6b62965e86df7aec5f))


### Features

* app installations ([79dafa8](https://github.com/jcloudify/jcloudify-api/commit/79dafa8e553f8db6c12862779749316f4600e9d5))
* get envionment by id ([e0d69ba](https://github.com/jcloudify/jcloudify-api/commit/e0d69bad398ebe788b8541434e24afd0f206ae81))
* poja cli integration with git repo push ([c22b8ba](https://github.com/jcloudify/jcloudify-api/commit/c22b8ba58d1534698d88e9e8ebbc274341e8a729))



