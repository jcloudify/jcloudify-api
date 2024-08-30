# [3.18.0](https://github.com/jcloudify/jcloudify-api/compare/v3.17.0...v3.18.0) (2024-08-30)


### Bug Fixes

* can create new environment after archiving one ([e2459cf](https://github.com/jcloudify/jcloudify-api/commit/e2459cfd51128cea570249b0775fb52c6d152d4b))


### Features

* archive stacks ([c8205cc](https://github.com/jcloudify/jcloudify-api/commit/c8205ccc7af8ae823a6e044ff7d5abb72557365e))
* delete cloudformation stack by name ([4a8a87d](https://github.com/jcloudify/jcloudify-api/commit/4a8a87d7069cc7c6052637044422664da47f9f0b))
* handle archiving applications ([3f4cf72](https://github.com/jcloudify/jcloudify-api/commit/3f4cf7201403aa1ebe678a28f60ea28fb5c64d67))



# [3.17.0](https://github.com/jcloudify/jcloudify-api/compare/v3.16.0...v3.17.0) (2024-08-29)


### Bug Fixes

* do not check compute stack status on stacks deployments ([738c02f](https://github.com/jcloudify/jcloudify-api/commit/738c02facaf11ca57a9de043771a0e470abf5335))
* ignore getEventStack in CheckTemplateIntegrityTriggered ([b241f0c](https://github.com/jcloudify/jcloudify-api/commit/b241f0c124107b360d6e13247a1ea66835a42897))
* LinkedHashMap type in stackDatas ([0ce42ae](https://github.com/jcloudify/jcloudify-api/commit/0ce42ae15b4c292ce1e3aa7461a3f825533dc231))
* return empty list if there's not stack events either stack outputs ([562f795](https://github.com/jcloudify/jcloudify-api/commit/562f795fa87adad185b1dd31ae0c62ea29285d70))
* stack data pagination ([d62a852](https://github.com/jcloudify/jcloudify-api/commit/d62a85238be93c165f31d051836fe7048d5f5ac7))
* stack events and stack outputs pagination ([1108f68](https://github.com/jcloudify/jcloudify-api/commit/1108f6846c2f8506bc0c6cd35a855daa659ea2f9))
* update jcloudify deployer event source pattern ([48a330f](https://github.com/jcloudify/jcloudify-api/commit/48a330f1e823ce3bb5d45720fdafbef1c7e6a446))


### Features

* check built template before processing to deployment ([e764127](https://github.com/jcloudify/jcloudify-api/commit/e76412759fdd63cec79656e499aae49a61ad67e6))
* get stack outputs ([e36d54d](https://github.com/jcloudify/jcloudify-api/commit/e36d54d846aef02b810851e8b751e0025b0c2344))
* only start deployment after template file check ([76e732d](https://github.com/jcloudify/jcloudify-api/commit/76e732d5095d7914f3577757ffe8e85a451a3e2b))
* save stack outputs ([1ca3f27](https://github.com/jcloudify/jcloudify-api/commit/1ca3f27a4a22e1c06e6f8e69e7cef072c9068970))
* set cli-version to 17.1.2 ([a8f0468](https://github.com/jcloudify/jcloudify-api/commit/a8f0468534cda8d554798605646ec6f14c6c33a2))



# [3.16.0](https://github.com/jcloudify/jcloudify-api/compare/v3.15.1...v3.16.0) (2024-08-23)


### Bug Fixes

* destination filename for cd-compute ([d7bd301](https://github.com/jcloudify/jcloudify-api/commit/d7bd301d211b670aabaad6de4caddf40c4fa5854))
* do not remove reconfigured cd-compute from code to be pushed ([4bccc55](https://github.com/jcloudify/jcloudify-api/commit/4bccc558225bb4897fb38dd368860755d9c9ddee))
* do not save event stack file if there's not and save sqlite stack file to s3 ([a16123c](https://github.com/jcloudify/jcloudify-api/commit/a16123c0594d34809b22105bfa063caf99fb50f8))
* environment variable is case sensitive ([5ad9467](https://github.com/jcloudify/jcloudify-api/commit/5ad946700b86b875b17cc7cb9be3791c918b2015))


### Features

* save stack events for compute stack ([471546b](https://github.com/jcloudify/jcloudify-api/commit/471546b9a308e508ca2f19cf21baf3dbd1480d4f))



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



