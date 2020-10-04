# Querydsl advanced

## querydsl
* 쿼리를 자바 코드로 작성이 가능하며, 문법오류를 컴파일 시점에 찾을 수 있다.
* 동적쿼리 가능

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

## Querydsl 에서 사용하는 jpql 검색 조건
```java
member.username.eq("member1") // username = 'member1'
member.username.ne("member1") //username != 'member1'
member.username.eq("member1").not() // username != 'member1'
member.username.isNotNull() //이름이 is not null
member.age.in(10, 20) // age in (10,20)
member.age.notIn(10, 20) // age not in (10, 20)
member.age.between(10,30) //between 10, 30
member.age.goe(30) // age >= 30
member.age.gt(30) // age > 30
member.age.loe(30) // age <= 30
member.age.lt(30) // age < 30
member.username.like("member%") //like 검색
member.username.contains("member") // like ‘%member%’ 검색
member.username.startsWith("member") //like ‘member%’ 검색

```