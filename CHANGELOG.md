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



# [3.7.0](https://github.com/jcloudify/jcloudify-api/compare/v3.6.0...v3.7.0) (2024-07-26)


### Features

* change custom_java_deps and custom_java_repositories from string map to string list ([3512a62](https://github.com/jcloudify/jcloudify-api/commit/3512a625ae501630ca508848a2bea4e43369f991))
* get application by id ([11363d8](https://github.com/jcloudify/jcloudify-api/commit/11363d8c2ac0a3c0edb5a257a6957c9c5a383305))
* get stack events ([b50655a](https://github.com/jcloudify/jcloudify-api/commit/b50655a18b519d084a568988d58906b50cddf803))



# [3.6.0](https://github.com/jcloudify/jcloudify-api/compare/v3.5.2...v3.6.0) (2024-07-26)


### Bug Fixes

* create log file in temp directory ([0cc9893](https://github.com/jcloudify/jcloudify-api/commit/0cc9893e24c3d3e53117cc6827e77607d596f2e8))
* crupdate stack events ([c855ba3](https://github.com/jcloudify/jcloudify-api/commit/c855ba3c87d8957d782f9b97fa42ae586dfe134f))
* security conf ([99629bf](https://github.com/jcloudify/jcloudify-api/commit/99629bf703cf36318918b3a35581430c2fd5699d))


### Features

* crupdate repo on application crupdate ([c9276f7](https://github.com/jcloudify/jcloudify-api/commit/c9276f7b28fc57cb7c67e1168f63bd1ee69daca3))
* poja version is hidden as internal, shown version is jcloudifys and will be mapped internally ([c8739ff](https://github.com/jcloudify/jcloudify-api/commit/c8739ff196468ee50f07f60f2e0ca63e0c85dacc))



## [3.5.2](https://github.com/jcloudify/jcloudify-api/compare/v3.5.1...v3.5.2) (2024-07-25)


### Bug Fixes

* bucket key path does not starts with a slash ([755e817](https://github.com/jcloudify/jcloudify-api/commit/755e8177ab5c3cc102f69da030051c8b60d5ea64))
* missing env var ([9bc347a](https://github.com/jcloudify/jcloudify-api/commit/9bc347a870c7f7acbc8c64a202d22f90d9c684a2))



## [3.5.1](https://github.com/jcloudify/jcloudify-api/compare/v3.5.0...v3.5.1) (2024-07-25)


### Bug Fixes

* **poja-conf:** poja custom are HashMaps ([73cd8a3](https://github.com/jcloudify/jcloudify-api/commit/73cd8a36ad9527f330ffcd0b80489e8a96559103))



# [3.5.0](https://github.com/jcloudify/jcloudify-api/compare/v3.4.0...v3.5.0) (2024-07-25)


### Bug Fixes

* remove duplicated stack id ([5618141](https://github.com/jcloudify/jcloudify-api/commit/561814123d2530148dfeffb72b4fa2cc6f85f27d))
* update stack when it already exists ([bda3bf6](https://github.com/jcloudify/jcloudify-api/commit/bda3bf6892a699f3789d7c608fd06ab8b1d29b7b))


### Features

* add repository description to application, and fix poja-conf tests ([28b8a46](https://github.com/jcloudify/jcloudify-api/commit/28b8a463c0264aae19b0d72dd7d678409e55c31d))
* PojaConfV17_0_0 ([d29fd7b](https://github.com/jcloudify/jcloudify-api/commit/d29fd7b7c3b5b5d8e3da50cbad18729bd9b5905b))
* save stack logs in s3 ([d97c8af](https://github.com/jcloudify/jcloudify-api/commit/d97c8af8427f4467a33196be61f81d20f84d082a))



