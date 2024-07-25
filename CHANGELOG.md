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



# [3.0.0](https://github.com/jcloudify/jcloudify-api/compare/v1.5.0...v3.0.0) (2024-07-03)


### chore

* set code version to 2.0.0 ([51ae182](https://github.com/jcloudify/jcloudify-api/commit/51ae182a71d23c29e4fd4fdb009b103823725abd))


### Features

* individually deploy event stack 1 and 2 ([6501251](https://github.com/jcloudify/jcloudify-api/commit/6501251c98c79265f17c257f9ba441cd3fb8128b))


### BREAKING CHANGES

* in doc/api.yml add Environment[]  to Application



# [1.5.0](https://github.com/jcloudify/jcloudify-api/compare/v1.4.0...v1.5.0) (2024-06-20)


### Features

* initiate cloudformation stack deployments ([1d851c8](https://github.com/jcloudify/jcloudify-api/commit/1d851c8a1c4a3071e8bba1a9ed22009484d0c6ed))
* initiate stack deployment ([c1f66ab](https://github.com/jcloudify/jcloudify-api/commit/c1f66ab47262147e8a240f07c3af094c91227a5a))



