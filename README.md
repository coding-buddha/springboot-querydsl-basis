# Querydsl advanced

## querydsl
* 쿼리를 자바 코드로 작성이 가능하며, 문법오류를 컴파일 시점에 찾을 수 있다.
* 동적쿼리 가능

<BR>

## Q-Type 을 사용 방법 두가지
```java
// 둘은 동일하다
QMember member = new QMember("m");      // 임의로 `m` 을 삽입 (구분하는 이름인데 중요하진 않다고 함)
QMember member = QMember.member;

JPAQueryFactory queryFactory = new JPAQueryFactory(em);

// static 임포트가 가능하다
final Member foundMember = queryFactory
        .selectFrom(member)
        .from(member)
        .where(member.username.eq("PARK SUNG DONG"))
        .fetchOne();
```

<BR>

## Querydsl 에서 사용하는 jpql 검색 조건
```java
member.username.eq("member1")           // username = 'member1'
member.username.ne("member1")           //username != 'member1'
member.username.eq("member1").not()     // username != 'member1'
member.username.isNotNull()             // 이름이 is not null
member.age.in(10, 20)                   // age in (10,20)
member.age.notIn(10, 20)                // age not in (10, 20)
member.age.between(10,30)               // between 10, 30 (10 과 30을 포함)
member.age.goe(30)                      // age >= 30
member.age.gt(30)                       // age > 30
member.age.loe(30)                      // age <= 30
member.age.lt(30)                       // age < 30
member.username.like("member%")         // like 검색
member.username.contains("member")      // like ‘%member%’ 검색
member.username.startsWith("member")    // like ‘member%’ 검색
```

<BR>

## Querydsl 결과조회
```java
fetch()         // 리스트 조회
fetchOne()      // 단 건 조회
                // |- 결과가 없으면 null
                // |- 결과가 둘 이상이면, com.querydsl.core.NonUniqueResultExcetpion
fetchFirst()    // limit(1).fetchOne()
fetchResults()  // 페이징 정보 조회. total count 포함 ( .getTotal() )
fetchCount()    // count 쿼리로 변경되어 count 수 조회
```

<BR>

## Querydsl aggregation
```java
// 생략
.groupBy(item.price)
.having(item.price.gt(1000))
```

<BR>

## Querydsl inner join 
```java
final List<Member> members = queryFactory
        .selectFrom(member)
        .join(member.team, team)
        .where(team.name.eq("YES TEAM"))
        .fetch();

// inner join 수행
select
    member0_.id as id1_0_,
    member0_.age as age2_0_,
    member0_.team_id as team_id4_0_,
    member0_.username as username3_0_ 
from
    member member0_ 
inner join
    team team1_ 
        on member0_.team_id=team1_.id 
where
    team1_.name=?
``` 

<BR>

## Querydsl left join
```java
final List<Member> members = queryFactory
        .selectFrom(member)
        .leftJoin(member.team, team)
        .where(team.name.eq("YES TEAM"))
        .fetch();

// left outer join
select
    member0_.id as id1_0_,
    member0_.age as age2_0_,
    member0_.team_id as team_id4_0_,
    member0_.username as username3_0_ 
from
    member member0_ 
left outer join
    team team1_ 
        on member0_.team_id=team1_.id 
where
    team1_.name=?
```

<BR>

## ON 절을 이용한 join
* 조인대상 필터링 
   * 외부조인인 경우에는 `on()` 절을 사용
   * 내부조인을 사용하고자 하는 경우에는 `join()`, `where()` 를 사용할 수 있도록 한다.
* 연관관계가 없는 엔티티에 대한 외부조인 가능
    * 아래의 `두 가지를 비교` 한다.
    * 일반조인 `leftJoin(member.team, team)` : `FK` 값을 연걸하는 형태이다.
    * on 조인 `from(member).leftJoin(team).on(member.username.eq(team.name))`

```java
final List<Tuple> tuples = queryFactory
        .select(member, team)
        .from(member)
        .leftJoin(member.team, team).on(team.name.eq("NEW TEAM"))
        .fetch();

select
    member0_.id as id1_0_0_,
    team1_.id as id1_1_1_,
    member0_.age as age2_0_0_,
    member0_.team_id as team_id4_0_0_,
    member0_.username as username3_0_0_,
    team1_.name as name2_1_1_ 
from
    member member0_ 
left outer join
    team team1_ 
        on member0_.team_id=team1_.id 
        and (
            team1_.name=?
        )

// 외부조인이기 때문에 null 값이 나오는데 null 값이 나오고자 하지 않는다면, 
// join() 메소드를 사용하여 내부조인을 사용할 수 있도록 한다.
// 아래은 그 예시

// 내부조인을 이용
final List<Tuple> tuples = queryFactory
        .select(member, team)
        .from(member)
        .join(member.team, team)
        .where(team.name.eq("NEW TEAM"))
        .fetch();
```

<BR>

## subQuery 이용하기
```java
// subquery 로 들어가는 부분에 대해서 새로운 별칭이 필요하다.
QMember memberSub = new QMember("memberSub");

// JPAExpressions 는 static import 가 가능하다.
final List<Member> members = queryFactory
        .selectFrom(member)
        .where(member.age.eq(
                JPAExpressions
                        .select(memberSub.age.max())
                        .from(memberSub)
        ))
        .fetch();

select
    member0_.id as id1_0_,
    member0_.age as age2_0_,
    member0_.team_id as team_id4_0_,
    member0_.username as username3_0_ 
from
    member member0_ 
where
    member0_.age=(
        select
            max(member1_.age) 
        from
            member member1_
    )
```
* from 절의 서브쿼리는 지원하지 않는다.
    * 서브쿼리를 join 으로 변경한다.
    * 애플리케이션 단에서 쿼리를 두 번 분리해서 실행한다.
    * `nativeSQL` 을 사용한다.

<BR>

## CASE 문 이용하기 (DB 에서 ROW 데이터를 필터링하거나 계산하는 부분은 최소한으로 수행한다.)
```java
final List<String> results = queryFactory
        .select(member.age
                .when(10).then("10대")
                .when(50).then("중반")
                .otherwise("저 세상"))
        .from(member)
        .fetch();


select
        case 
            when member0_.age=? then ? 
            when member0_.age=? then ? 
            else '저 세상' 
        end as col_0_0_ 
    from
        member member0_
```

<BR>

## Query Projection 이용하기 Dto 변환을 수행한다.
* 3가지 방법이 존재한다.
    * Setter
    * Field
    * Constructor

```java
@Test
@DisplayName("jpql 이용, query projection 이용하기, Dto 반환")
public void findDtoByJPQL() {
    // 생성자 처럼 이용한다.
    // new operation 을 활용하여 패키지 경로까지 다 적어주어야 하기 때문에 지저분해진다.
    /** 조회하려는 필드의 생성자만 있으면 된다. **/
    final List<MemberDto> memberDtos = em.createQuery("select new edu.pasudo123.study.demo.member.MemberDto(m.username, m.age) from Member m", MemberDto.class)
            .getResultList();

    memberDtos.forEach(System.out::println);
}

@Test
@DisplayName("querydsl 이용, Dto 반환 [Setter] 이용")
public void findDtoByQuerydslSetter() {
    final List<MemberDto> memberDtos = queryFactory
            // getter & setter & default constructor 필요
            // 접근지정자는 public 으로 수행한다.
            .select(Projections.bean(MemberDto.class, member.username, member.age))
            .from(member)
            .fetch();

    memberDtos.forEach(System.out::println);
}

@Test
@DisplayName("querydsl 이용, Dto 반환 [Field] 이용")
public void findDtoByQuerydslField() {
    final List<MemberDto> memberDtos = queryFactory
            // getter & setter & default constructor 필요
            .select(Projections.fields(MemberDto.class, member.username, member.age))
            .from(member)
            .fetch();

    memberDtos.forEach(System.out::println);
}

@Test
@DisplayName("querydsl 이용, Dto 반환 [Constructor] 이용")
public void findDtoByQuerydslConstructor() {
    final List<MemberDto> memberDtos = queryFactory
            // getter & setter & required constructor 필요
            .select(Projections.constructor(MemberDto.class, member.username, member.age))
            .from(member)
            .fetch();

    memberDtos.forEach(System.out::println);
}

@Test
@DisplayName("querydsl 이용, Dto 반환 as 키워드 이용")
public void findDtoByQuerydslAsKeyword() {
    final List<UserDto> userDtos = queryFactory
            // getter & setter & required constructor 필요
            .select(Projections.constructor(UserDto.class, member.username.as("name"), member.age))
            .from(member)
            .fetch();

    userDtos.forEach(System.out::println);

@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class MemberDto {

    private String username;
    private int age;

    public MemberDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}

@ToString
@Getter
public class UserDto {
    private String name;
    private int age;

    public UserDto(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
```

<BR>

## @QueryProjection 을 이용한 Dto 조회
```java
@Test
@DisplayName("querydsl 이용, @QueryProjection annotation 이용을 한다.")
public void findDtoByQueryProjectionAnnotation() {
    /**
     * compile querydsl task 를 수행시켜서 dto 에 관한 QClass 를 만들어준다.
     * task compileQuerydsl
     *
     * Querydsl 로 만들어놓은 dto 생성자에서 추가적인 필드를 넣으면 컴파일 에러가 발생한다.
     * 나머지 위에 setter, field, constructor 는 컴파일타임에 에러가 확인되지 않고 런타임에서 에러를 확인된다.
     *
     * [단점]
     * Dto 에 querydsl 관련된 코드가 의존적으로 들어갈 수 있다.
     * 해당 Dto 가 각층의 레이어에서 쓰이게 된다면 @QueryProjection 가 여기저기 돌아다니게 된다.
     * **/
    final List<AccountDto> accountDtos = queryFactory
            .select(new QAccountDto(member.username, member.age))
            .from(member)
            .fetch();

    accountDtos.forEach(System.out::println);
}

@Getter
@ToString
public class AccountDto {

    private String username;
    private int age;

    @QueryProjection
    public AccountDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
``` 