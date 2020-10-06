package edu.pasudo123.study.demo.member;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import edu.pasudo123.study.demo.team.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;

import static com.querydsl.jpa.JPAExpressions.*;
import static edu.pasudo123.study.demo.member.QMember.member;
import static edu.pasudo123.study.demo.team.QTeam.team;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Member 엔티티 테스트 클래스는")
@ActiveProfiles("test")
@Transactional
class MemberBasisTest {

    @Autowired
    private EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    @DisplayName("Member 엔티티를 선행으로 삽입한다.")
    public void init() {
        queryFactory = new JPAQueryFactory(em);

        // given
        em.persist(Member.builder()
                .username("PARK SUNG DONG")
                .age(29)
                .team(Team.builder().name("NEW TEAM").build())
                .build());
        em.persist(Member.builder()
                .username("SON")
                .age(29)
                .team(Team.builder().name("YES TEAM").build())
                .build());
        em.persist(Member.builder()
                .username("CHA")
                .age(51)
                .team(Team.builder().name("NEW TEAM").build())
                .build());
        em.persist(Member.builder()
                .username("ABA")
                .age(51)
                .team(Team.builder().name("NEW TEAM").build())
                .build());

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("jpql 을 통해서 데이터를 조회한다.")
    public void jpqlTest() {

        final String query = "SELECT m FROM Member m WHERE m.username = :username";

        // when
        final Member foundMember = em.createQuery(query, Member.class)
                .setParameter("username", "PARK SUNG DONG")
                .getSingleResult();

        // then
        assertThat(foundMember.getUsername()).isEqualTo("PARK SUNG DONG");
    }

    @Test
    @DisplayName("querydsl 을 통해서 데이터를 조회한다.")
    public void querydslTest() {

        // when
        final Member foundMember = queryFactory
                .selectFrom(member)
                .from(member)
                .where(member.username.eq("PARK SUNG DONG"))
                .fetchOne();

        // then
        assert foundMember != null;
        assertThat(foundMember.getUsername()).isEqualTo("PARK SUNG DONG");
    }

    @Test
    @DisplayName("querydsl 을 통해 검색 조회를 수행한다.")
    public void querydslSearchTest() {

        // when
        final Member foundMember = queryFactory
                .selectFrom(member)
                .where(
//                        member.username.eq("PARK SUNG DONG")
//                                .and(member.age.eq(30)))
                        member.username.eq("PARK SUNG DONG"),
                        member.age.between(20, 31)
                )
                .fetchOne();

        // then
        assert foundMember != null;
        assertThat(foundMember.getUsername()).isEqualTo("PARK SUNG DONG");
    }

    @Test
    @DisplayName("querydsl 을 통해 정렬 조회를 수행한다.")
    public void querydslSortTest() {

        // when
        final List<Member> results = queryFactory
                .selectFrom(member)
                .where(member.age.lt(100))
                .orderBy(member.age.desc(), member.username.asc().nullsFirst())
                .fetch();

        // then
        assertThat(results.size()).isNotZero();
    }

    @Test
    @DisplayName("querydsl 을 통해 페이징 조회를 수행한다.")
    public void querydslPagingTest() {
        final List<Member> members = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(0)  // offset 은 0부터
                .limit(1)
                .fetch();

        // then
        // order by member.username desc limit ? offset ?
        assertThat(members.size()).isNotZero();
    }

    @Test
    @DisplayName("querydsl 을 이용하여 집함 함수를 이용한다.")
    public void querydslAggregationTest() {

        // when
        final List<Tuple> tuples = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetch();

        // then
        // 여러 개의 타입을 꺼내올 수 있다.
        Tuple tuple = tuples.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isGreaterThan(0);
    }

    @Test
    @DisplayName("팀의 이름과 각 팀의 연령을 구하기")
    public void querydslGroupTest() {

        // given
        em.persist(Member.builder()
                .username("ABA")
                .age(51)
                .team(Team.builder().name("YES TEAM").build())
                .build());
        em.flush();
        em.clear();

        // when
        List<Tuple> tuples = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        // then
        Tuple teamA = tuples.get(0);
        Tuple teamB = tuples.get(1);
    }

    @Test
    @DisplayName("조인하기")
    public void querydslJoinTest() {

        // given
        final List<Member> members = queryFactory
                .selectFrom(member)
                .leftJoin(member.team, team)
                .where(team.name.eq("YES TEAM"))
                .fetch();

        // when
        assertThat(members)
                .extracting("username")
                .containsExactly("SON");
    }

    @Test
    @DisplayName("조인 ON 절을 이용하기")
    public void querydslJoinOnTest() {

        // given
        final List<Tuple> tuples = queryFactory
                .select(member, team)
                .from(member)
                .join(member.team, team)
                .where(team.name.eq("NEW TEAM"))
                .fetch();

        // when
        for(Tuple tuple : tuples) {
            System.out.println("tuples : " + tuple);
        }
    }

    @Test
    @DisplayName("나이가 가장 많은 회원 조회")
    public void querydslSubQuery() {

        // subquery 로 들어가는 부분에 대해서 새로운 별칭이 필요하다.
        QMember memberSub = new QMember("memberSub");

        final List<Member> members = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(members).extracting("age")
                .hasSizeGreaterThan(0);
    }

    @Test
    @DisplayName("case 조건절을 이용한 query 수행")
    public void querydslCaseQuery() {
        final List<String> results = queryFactory
                .select(member.age
                        .when(10).then("10대")
                        .when(50).then("중반")
                        .otherwise("저 세상"))
                .from(member)
                .fetch();

        assertThat(results.size()).isGreaterThan(0);
    }

    @Test
    @DisplayName("concat 이용해서 querydsl 수행")
    public void querydslConcatQuery() {

        // .stringValue 를 사용할 일이 많다.
        final List<String> results = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .fetch();

        for(String concatResult : results) {
            System.out.println(concatResult);
        }
    }
}