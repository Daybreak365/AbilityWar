# 통합 JAR 패키징

일반적인 전체 소스 빌드는 아래 안내에 따라 각 모듈의 서버 의존성을 준비한 후 저장소 루트에서 `mvn -Dgpg.skip=true clean verify`로 실행합니다. 출력은 `modules/Jar/target/AbilityWar-26.0.1.jar`이며, 1.12.2부터 26.3까지의 정식 릴리스 계열에 대응하는 총 28개 호환 모듈을 포함합니다. 서버 종류와 버전별 검사 범위는 아래 검증 기록을 확인하세요.

`minecraft-26.3` 프로필은 기존 모듈을 제외하는 개발 검사용 구성입니다. 이때 출력은 `AbilityWar-26.0.1-mc26.3.jar`로 구분됩니다. 최종 JAR 검사는 통합 구성의 모든 호환 모듈과 1.12용 광대·은둔자·허수아비·소울 클래스가 포함되어 있는지 확인합니다.

## 공식 배포본의 변경되지 않은 호환 모듈 재사용

과거 CraftBukkit 빌드 의존성이 없는 환경에서는 공식 3.6.0 배포본의 기존 호환 클래스만 재사용할 수 있습니다. 공통 코드, 수정된 1.17·1.19·1.20.4 모듈, 추가된 15개 모듈 및 26.3 모듈은 현재 소스로 다시 빌드합니다. 이전 배포본의 공통 코드를 섞지 않습니다.

1. 공식 [20260804 릴리스](https://github.com/Daybreak365/AbilityWar/releases/tag/20260804)의 `AbilityWar-3.6.0.jar`를 준비합니다.
2. `git fetch origin tag 20260804`로 기준 소스를 가져옵니다.
3. `python3 tools/install-legacy-adapters.py /path/to/AbilityWar-3.6.0.jar`를 실행합니다. 별도 Maven 로컬 저장소를 쓰는 경우 `--repository /path/to/repository`를 지정합니다.
4. JDK 21과 JDK 25 toolchain을 준비하고 다음 명령을 실행합니다.

```sh
mvn -pl :Common,:Compatibility,:Patch,:Plugin,:v1_17_R1,:v1_19_R1,:v1_19_R2,:v1_20_R3,:v1_18_R1,:v1_18_R2,:v1_19_R3,:v1_20_R1,:v1_20_R2,:v1_20_R4,:v1_21_R1,:v1_21_R2,:v1_21_R3,:v1_21_R4,:v1_21_R5,:v1_21_R6,:v1_21_R7,:v26_1,:v26_2,:v26_3,:Jar -Dgpg.skip=true clean verify
```

이 명령에는 `-P minecraft-26.3` 또는 `-am`을 붙이지 않습니다. 기본 통합 프로필을 유지하며 변경되지 않은 기존 8개 호환 모듈은 검증된 로컬 아티팩트로 사용합니다. 화살 메타데이터 및 NMS 인터페이스 오류를 수정한 1.17·1.19·1.20.4 모듈은 현재 소스로 빌드하며 과거 배포본에서 가져오지 않습니다.

스크립트는 공식 JAR의 SHA-256(`3bf1b4cb445cf3bdd0f1e3e81dcbe864c5e49c29b8fc64b28dff0e3f63dfe720`)과 `20260804` 기준의 기존 모듈·Common 소스 일치를 확인합니다. 관련 소스가 변경되었으면 재사용을 거부하므로 전체 소스 빌드를 사용해야 합니다. 생성된 호환 모듈 POM에는 원본 배포본의 태그와 체크섬이 기록됩니다.

## 1.12.2 서버 검사

`modules/Jar/integration-test/legacy`는 별도 테스트 플러그인입니다. Java 8로 컴파일하며, 엔티티를 생성하고 종료 시 서버를 정지하므로 전용 테스트 월드에서만 실행합니다.

```sh
JAVA_HOME=/path/to/jdk8 bash modules/Jar/integration-test/legacy/build.sh /path/to/patched_1.12.2.jar
```

통합 JAR과 생성된 `modules/Jar/target/legacy-integration-test/LegacySmoke.jar`를 Paper 1.12.2 테스트 서버에 넣고 Java 8로 실행합니다. `LEGACY SMOKE RESULT`의 실패 수와 개별 결과를 확인합니다. 서버 프로세스의 종료 코드만으로 성공을 판단하지 않습니다. 검사 대상은 버전별 능력 등록, 커스텀 머리, 홀로그램, 흡수 체력, 더미, 마그넷 이동·정리, 공포, GUI 페이지 변경입니다. 26.3 검사는 [기존 안내](../modules/compatibility/v26_3/README.md)를 따릅니다.

이전 13개 모듈 통합본 검증 기록: 통합 구성의 Maven 검사 48개, Paper 1.12.2 build 1620 / Java 8 서버 검사 16개, Paper 26.3 build 222 / Java 25 서버 검사 43개가 통과했습니다. 기존 12개 모듈의 클래스 387개와 현재 공통·26.3 클래스는 각각 원본과 바이트 단위로 일치합니다. 26.3 전용 JAR을 통합 구성으로 검사하면 `v1_12_R1/NMSImpl.class` 누락으로 실패하는 것도 확인했습니다. 모든 중간 버전 서버와 외부 애드온의 전체 동작을 실행 검증한 것은 아닙니다.


## CraftBukkit 1.18–1.20.4 의존성

새로 추가하거나 수정한 `v1_18_R1`, `v1_18_R2`, `v1_19_R1`, `v1_19_R2`, `v1_19_R3`, `v1_20_R1`, `v1_20_R2`, `v1_20_R3`는 각각 1.18.1, 1.18.2, 1.19.2, 1.19.3, 1.19.4, 1.20.1, 1.20.2, 1.20.4의 공식 BuildTools 결과를 사용합니다. 해당 버전에 맞는 JDK로 `java -jar BuildTools.jar --rev <version> --compile CRAFTBUKKIT`를 실행하세요.

이 모듈의 소스는 Spigot 클래스 이름과 매핑된 멤버 이름을 사용합니다. BuildTools가 남기는 다음 중간 산출물을 `remapped-members` classifier로 추가 설치합니다. 기본 서버 JAR이나 POM을 덮어쓰지 않습니다. 아래 명령은 1.19.4 예시이며, 프로젝트와 같은 Maven 로컬 저장소를 사용해야 합니다.

```sh
mvn install:install-file \
  -Dfile=/path/to/build/CraftBukkit/target/original-craftbukkit-1.19.4-R0.1-SNAPSHOT.jar \
  -DgroupId=org.bukkit -DartifactId=craftbukkit -Dversion=1.19.4-R0.1-SNAPSHOT \
  -Dpackaging=jar -Dclassifier=remapped-members -DgeneratePom=false
```

## 추가된 1.20.5–26.2 의존성 및 매핑

1.20.5 이후 새 모듈은 공식 Paper 번들의 Mojang 이름을 사용해 컴파일합니다. 준비 도구에는 Python 3.9 이상이 필요합니다. 다음 대표 버전의 번들을 준비합니다: 1.20.6, 1.21.1, 1.21.3, 1.21.4, 1.21.5, 1.21.8, 1.21.10, 1.21.11, 26.1.2, 26.2.

```sh
python3 tools/paper-matrix.py --directory /path/to/bundles \
  --java /path/to/jdk21/bin/java 1.20.6 1.21.1 1.21.3 1.21.4 1.21.5 1.21.8 1.21.10 1.21.11
python3 tools/paper-matrix.py --directory /path/to/bundles \
  --java /path/to/jdk25/bin/java 26.1.2 26.2
python3 tools/install-paper-bundle.py /path/to/bundles/1.20.6
```

마지막 설치 명령을 각 번들 디렉토리에 반복합니다. `--maven-repo`로 프로젝트가 사용하는 로컬 저장소를 지정할 수 있습니다. 설치 도구는 공식 빌드 메타데이터의 번들 SHA-256 및 번들에 기록된 서버·라이브러리 체크섬을 검사합니다. `paper-server`는 이 도구가 설치하는 로컬 컴파일 의존성이며, 공개 Maven 저장소에서 직접 내려받는 아티팩트가 아닙니다. 서버 라이브러리는 `provided` 범위로 사용하므로 최종 플러그인에 포함되지 않습니다.

1.20.5–1.21.11 모듈은 패키징 시 번들에 포함된 공식 매핑으로 Spigot 이름과 버전별 CraftBukkit 패키지로 변환합니다. Spigot·CraftBukkit은 이 결과를 직접 사용하고, Paper는 서버 내장 변환기로 Mojang 이름으로 되돌립니다. 따라서 배포 JAR 전체에 `paperweight-mappings-namespace: mojang`을 지정하지 않습니다. 26.x 모듈은 서버가 공식 이름을 그대로 사용하므로 이 역변환을 적용하지 않습니다.

컴파일에는 JDK 25 toolchain을 사용하며, 새 모듈의 바이트코드는 Java 21을 대상으로 합니다. 이전 Paper의 변환기가 통합 JAR 안의 26.x 클래스를 읽을 수 있게 하기 위한 설정입니다. **26.x 서버 실행에는 여전히 Java 25가 필요합니다.** 공통 코드와 기존 구버전 모듈은 Java 8 대상입니다.

서버 검사는 [공통 검사 도구](../modules/Jar/integration-test/matrix/README.md)를 사용합니다. 공식 서버 아카이브가 없는 마이너 릴리스와, BuildTools가 같은 계열의 최신 패치로 연결하는 릴리스는 별도의 실행 검증 결과로 취급하지 않습니다.

### 검증용 CraftBukkit 1.20.6 빌드 기록

Maven 저장소 연결 장애로 일부 상위 POM을 받을 수 없어, 테스트용 CraftBukkit 체크아웃에서 OSHI·Log4j 의존성의 메타데이터만 검증된 로컬 좌표로 바꿨습니다. 공식 Java 소스와 요청된 라이브러리 버전·JAR 바이트는 유지했습니다. 공식 BuildData에 포함된 SpecialSource 1.11.4로 기존 설정과 동일하게 멤버 이름을 변환한 뒤, Maven이 결정한 실행 클래스패스로 CraftBukkit의 `Main`을 실행했습니다. AbilityWar 통합 JAR은 저장소의 Maven 검증을 통과한 동일 파일을 사용했습니다. 이 서버에서 최종 통합 JAR의 공통 검사 31개와 홀로그램 문자열 검사 1개가 통과했습니다. Spigot 1.20.6은 별도 의존성 다운로드 장애로 빌드를 완료하지 못했으므로 실행 검증 결과에 포함하지 않습니다.

28개 모듈 통합본의 최종 검사 결과와 세부 버전별 검증 범위는 [호환 검증 기록](compatibility-validation.md)을 참고하세요.
