# 통합 JAR 패키징

일반적인 전체 소스 빌드는 각 버전의 CraftBukkit 의존성을 준비한 후 저장소 루트에서 `mvn -Dgpg.skip=true clean verify`로 실행합니다. 출력은 `modules/Jar/target/AbilityWar-26.0.0.jar`이며, 1.12.2부터 기존에 지원하던 각 버전과 26.3의 총 13개 호환 모듈을 포함합니다. 중간의 모든 Minecraft 릴리스를 새로 지원한다는 의미는 아닙니다.

`minecraft-26.3` 프로필은 기존 모듈을 제외하는 개발 검사용 구성입니다. 이때 출력은 `AbilityWar-26.0.0-mc26.3.jar`로 구분됩니다. 최종 JAR 검사는 통합 구성의 모든 호환 모듈과 1.12용 광대·은둔자·허수아비·소울 클래스가 포함되어 있는지 확인합니다.

## 공식 배포본의 변경되지 않은 호환 모듈 재사용

과거 CraftBukkit 빌드 의존성이 없는 환경에서는 공식 3.6.0 배포본의 기존 호환 클래스만 재사용할 수 있습니다. 공통 코드와 26.3 모듈은 현재 소스로 다시 빌드합니다. 이전 배포본의 공통 코드를 섞지 않습니다.

1. 공식 [20260804 릴리스](https://github.com/Daybreak365/AbilityWar/releases/tag/20260804)의 `AbilityWar-3.6.0.jar`를 준비합니다.
2. `git fetch origin tag 20260804`로 기준 소스를 가져옵니다.
3. `python3 tools/install-legacy-adapters.py /path/to/AbilityWar-3.6.0.jar`를 실행합니다. 별도 Maven 로컬 저장소를 쓰는 경우 `--repository /path/to/repository`를 지정합니다.
4. JDK 21과 JDK 25 toolchain을 준비하고 다음 명령을 실행합니다.

```sh
mvn -pl :Common,:Compatibility,:Patch,:Plugin,:v26_3,:Jar -Dgpg.skip=true clean verify
```

이 명령에는 `-P minecraft-26.3` 또는 `-am`을 붙이지 않습니다. 기본 통합 프로필을 유지하며 기존 12개 호환 모듈은 검증된 로컬 아티팩트로 사용합니다.

스크립트는 공식 JAR의 SHA-256(`3bf1b4cb445cf3bdd0f1e3e81dcbe864c5e49c29b8fc64b28dff0e3f63dfe720`)과 `20260804` 기준의 기존 모듈·Common 소스 일치를 확인합니다. 관련 소스가 변경되었으면 재사용을 거부하므로 전체 소스 빌드를 사용해야 합니다. 생성된 호환 모듈 POM에는 원본 배포본의 태그와 체크섬이 기록됩니다.

## 1.12.2 서버 검사

`modules/Jar/integration-test/legacy`는 별도 테스트 플러그인입니다. Java 8로 컴파일하며, 엔티티를 생성하고 종료 시 서버를 정지하므로 전용 테스트 월드에서만 실행합니다.

```sh
JAVA_HOME=/path/to/jdk8 bash modules/Jar/integration-test/legacy/build.sh /path/to/patched_1.12.2.jar
```

통합 JAR과 생성된 `modules/Jar/target/legacy-integration-test/LegacySmoke.jar`를 Paper 1.12.2 테스트 서버에 넣고 Java 8로 실행합니다. `LEGACY SMOKE RESULT`의 실패 수와 개별 결과를 확인합니다. 서버 프로세스의 종료 코드만으로 성공을 판단하지 않습니다. 검사 대상은 버전별 능력 등록, 커스텀 머리, 홀로그램, 흡수 체력, 더미, 마그넷 이동·정리, 공포, GUI 페이지 변경입니다. 26.3 검사는 [기존 안내](../modules/compatibility/v26_3/README.md)를 따릅니다.

검증 기록: 통합 구성의 Maven 검사 48개, Paper 1.12.2 build 1620 / Java 8 서버 검사 16개, Paper 26.3 build 222 / Java 25 서버 검사 43개가 통과했습니다. 기존 12개 모듈의 클래스 387개와 현재 공통·26.3 클래스는 각각 원본과 바이트 단위로 일치합니다. 26.3 전용 JAR을 통합 구성으로 검사하면 `v1_12_R1/NMSImpl.class` 누락으로 실패하는 것도 확인했습니다. 모든 중간 버전 서버와 외부 애드온의 전체 동작을 실행 검증한 것은 아닙니다.
