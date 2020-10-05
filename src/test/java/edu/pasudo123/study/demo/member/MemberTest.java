package edu.pasudo123.study.demo.member;

import com.querydsl.core.Tuple;
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

import static edu.pasudo123.study.demo.member.QMember.member;
import static edu.pasudo123.study.demo.team.QTeam.team;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Member 엔티티 테스트 클래스는")
@ActiveProfiles("test")
@Transactional
class MemberTest {

    @Autowired
    private EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    @DisplayName("Member 엔티티를 선행으로 삽입한다.")
    public void init() {
        queryFactory = new JPAQueryFactory(em);

        Member member = Member.builder()
                .username("PARK SUNG DONG")
                .age(29)
                .team(Team.builder().name("NEW TEAM").build())
                .build();

        em.persist(member);
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
        // given
        em.persist(Member.builder()
                .username("SON")
                .age(29)
                .team(Team.builder().name("NEW TEAM").build())
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

        // given
        em.persist(Member.builder()
                .username("ABA")
                .age(51)
                .team(Team.builder().name("NEW TEAM").build())
                .build());
        em.flush();
        em.clear();

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
        assertThat(tuple.get(member.count())).isEqualTo(2);
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
}