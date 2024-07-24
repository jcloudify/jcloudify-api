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



# [1.4.0](https://github.com/jcloudify/jcloudify-api/compare/v1.3.0...v1.4.0) (2024-06-11)


### Features

* user must provide email adress during signup ([c02f97d](https://github.com/jcloudify/jcloudify-api/commit/c02f97db67c5ca5c4baf387a2ff55a4534db55fd))



# [1.3.0](https://github.com/jcloudify/jcloudify-api/compare/v1.2.0...v1.3.0) (2024-06-04)


### Features

* avatar field to user data ([596a495](https://github.com/jcloudify/jcloudify-api/commit/596a4956bd0b4ce5a8391228c6597933be5b220d))
* user sign up endpoint ([df971a6](https://github.com/jcloudify/jcloudify-api/commit/df971a688a7d024dc95a89f139d1c7efb0b1215d))
* whoami endpoint ([866b901](https://github.com/jcloudify/jcloudify-api/commit/866b90109b06670a6a5230c944df343fa8376e8a))



# [1.2.0](https://github.com/jcloudify/jcloudify-api/compare/v1.1.0...v1.2.0) (2024-05-23)


### Bug Fixes

* rename user role attributea and default value ([45552a1](https://github.com/jcloudify/jcloudify-api/commit/45552a1ebd05f88ddf3086f9244dd5e3158e705b))


### Features

* configure github authentication ([7d9519c](https://github.com/jcloudify/jcloudify-api/commit/7d9519c58844e615b9149a3ed8e3e97e076378cf))
* implement endpoint to exchange github code into token ([5436877](https://github.com/jcloudify/jcloudify-api/commit/543687714d676d72d73cb8a4225380e1ce4b9616))



