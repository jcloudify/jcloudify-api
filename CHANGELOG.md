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



# [3.4.0](https://github.com/jcloudify/jcloudify-api/compare/v3.3.0...v3.4.0) (2024-07-24)


### Features

* configure application environment ([3e47e95](https://github.com/jcloudify/jcloudify-api/commit/3e47e9571e1175def69d0ef6bafe2b6925be4d9e))
* github repository name and scope(private or public) ([f5b86db](https://github.com/jcloudify/jcloudify-api/commit/f5b86db8c88b5c45b13a422daa9e578f62d513f4))



# [3.3.0](https://github.com/jcloudify/jcloudify-api/compare/v3.2.0...v3.3.0) (2024-07-24)


### Features

* get stack by id ([7834ded](https://github.com/jcloudify/jcloudify-api/commit/7834ded5c984df186fc45aa55c304b1bb29dedb1))
* **to-continue:** get stack list ([fe737eb](https://github.com/jcloudify/jcloudify-api/commit/fe737eb0d36ea7291abca2dd413312fe757f411d))



# [3.2.0](https://github.com/jcloudify/jcloudify-api/compare/v3.1.0...v3.2.0) (2024-07-23)


### Bug Fixes

* check user github id by token instead of email ([7751c5b](https://github.com/jcloudify/jcloudify-api/commit/7751c5b51373b7fba2d06d7c40d3005825c96546))


### Features

* add humanReadableVersion to PojaVersion in order to have the same string representation over api and consumers ([d919513](https://github.com/jcloudify/jcloudify-api/commit/d919513b777b4a77090ea4d7c342884e14294eb2))
* create and list environments, created environments have UNKNOWN state by default ([fa337e4](https://github.com/jcloudify/jcloudify-api/commit/fa337e44778195606b3bc4fd3bf117812d60cd80))
* get all applications filtered by name and userId ([04b42fa](https://github.com/jcloudify/jcloudify-api/commit/04b42fa63dbda469ef05243a222aeb8e53f52320))
* pricing is directly attached to user, defaulting to TEN_MICRO, there is no other ref ([301a12e](https://github.com/jcloudify/jcloudify-api/commit/301a12e5c4ffe04235744484d017a06045406420))



# [3.1.0](https://github.com/jcloudify/jcloudify-api/compare/v3.0.1...v3.1.0) (2024-07-16)


### Features

* can crupdate applications at PUT /applications ([1ff2575](https://github.com/jcloudify/jcloudify-api/commit/1ff2575f40bdc329ef22993847ae7a4db326f08f))
* list poja-versions read from resources/files/poja_versions.json ([ee6d7f5](https://github.com/jcloudify/jcloudify-api/commit/ee6d7f5ab1d5b739424f0f5d763e3bcbcf880784))



## [3.0.1](https://github.com/jcloudify/jcloudify-api/compare/v3.0.0...v3.0.1) (2024-07-10)


### Bug Fixes

* replace old stack type EVENT_1 and EVENT_2 by only EVENT ([def3e95](https://github.com/jcloudify/jcloudify-api/commit/def3e95bf23a7161ddc96a45763e74b449b56b36))



