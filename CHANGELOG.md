# Changelog

## 1.0.0 (2026-01-12)


### Features

* add all compose subcommand implementations ([#31](https://github.com/joshrotenberg/docker-wrapper-kotlin/issues/31)) ([b67b23f](https://github.com/joshrotenberg/docker-wrapper-kotlin/commit/b67b23f821444ea641aabe02db1c4bf87a25ff8a))
* add container templates for PostgreSQL, MySQL, MongoDB, RabbitMQ, and Nginx ([#39](https://github.com/joshrotenberg/docker-wrapper-kotlin/issues/39)) ([3c39d9c](https://github.com/joshrotenberg/docker-wrapper-kotlin/commit/3c39d9c02f3560199a7a7bebe9c2cabbf6ce26f8)), closes [#8](https://github.com/joshrotenberg/docker-wrapper-kotlin/issues/8)
* add context, manifest commands and complete RunCommand options ([#32](https://github.com/joshrotenberg/docker-wrapper-kotlin/issues/32)) ([de34789](https://github.com/joshrotenberg/docker-wrapper-kotlin/commit/de34789aa5908e3dd3f3ab15c895b46157efc870)), closes [#26](https://github.com/joshrotenberg/docker-wrapper-kotlin/issues/26)
* add Docker CLI commands and Compose DSL ([c897f26](https://github.com/joshrotenberg/docker-wrapper-kotlin/commit/c897f26ca12cb183e2946670e079ce1537eb5cef))
* add Docker Swarm commands and fix ExecCommand exit code handling ([#33](https://github.com/joshrotenberg/docker-wrapper-kotlin/issues/33)) ([fcd6938](https://github.com/joshrotenberg/docker-wrapper-kotlin/commit/fcd6938b51706e1084a06025ed9657611e93625f)), closes [#7](https://github.com/joshrotenberg/docker-wrapper-kotlin/issues/7) [#25](https://github.com/joshrotenberg/docker-wrapper-kotlin/issues/25)
* add Java builder API for Compose DSL ([c2829d7](https://github.com/joshrotenberg/docker-wrapper-kotlin/commit/c2829d740ef5a27e7e30bf00e9f52e891bc7e939))
* add JUnit 5 extension support and testing documentation ([431ae3b](https://github.com/joshrotenberg/docker-wrapper-kotlin/commit/431ae3bdb428c85a92766aebc9ed8d7c061870ce))
* add network, volume, system, auth commands and CI workflows ([#1](https://github.com/joshrotenberg/docker-wrapper-kotlin/issues/1)) ([3210d25](https://github.com/joshrotenberg/docker-wrapper-kotlin/commit/3210d25a41558b3717222c7c0500a67eec6b39b4))
* add retry support and container lifecycle management ([#49](https://github.com/joshrotenberg/docker-wrapper-kotlin/issues/49)) ([b332827](https://github.com/joshrotenberg/docker-wrapper-kotlin/commit/b332827a3f12e41b30c166db78e4648f1e2088d4))
* add streaming output support for long-running commands ([#42](https://github.com/joshrotenberg/docker-wrapper-kotlin/issues/42)) ([7d027e6](https://github.com/joshrotenberg/docker-wrapper-kotlin/commit/7d027e6d5eb858c6d0122346c539170cafa9d49d)), closes [#35](https://github.com/joshrotenberg/docker-wrapper-kotlin/issues/35)
* add typed responses, complete Docker facade, and fix timeout handling ([#37](https://github.com/joshrotenberg/docker-wrapper-kotlin/issues/37)) ([0be6785](https://github.com/joshrotenberg/docker-wrapper-kotlin/commit/0be67850af667f282a52a6987679207ff6b2e76c)), closes [#20](https://github.com/joshrotenberg/docker-wrapper-kotlin/issues/20) [#27](https://github.com/joshrotenberg/docker-wrapper-kotlin/issues/27) [#29](https://github.com/joshrotenberg/docker-wrapper-kotlin/issues/29)
* complete Docker command coverage ([#41](https://github.com/joshrotenberg/docker-wrapper-kotlin/issues/41)) ([c257f0d](https://github.com/joshrotenberg/docker-wrapper-kotlin/commit/c257f0d495f5842c6f8f87fbc8505f8baa9a3747))
* initial project structure with core, compose, templates, redis, and testing modules ([242cdc1](https://github.com/joshrotenberg/docker-wrapper-kotlin/commit/242cdc199f96c68204ce60eb72552c1fd6939553))


### Bug Fixes

* add gradle-wrapper.jar for CI builds ([#12](https://github.com/joshrotenberg/docker-wrapper-kotlin/issues/12)) ([89aff64](https://github.com/joshrotenberg/docker-wrapper-kotlin/commit/89aff64a7684e04ad99f2720a9ffda8c2bf6294c))
* address critical issues from code review ([#30](https://github.com/joshrotenberg/docker-wrapper-kotlin/issues/30)) ([c87cfd0](https://github.com/joshrotenberg/docker-wrapper-kotlin/commit/c87cfd0964aeff4a7855bae8ed83670c94b96797))
* skip Docker-dependent tests when Docker unavailable ([#19](https://github.com/joshrotenberg/docker-wrapper-kotlin/issues/19)) ([d1ebcae](https://github.com/joshrotenberg/docker-wrapper-kotlin/commit/d1ebcaee0a832f763e46cefbb44e090b1c54c89c))
