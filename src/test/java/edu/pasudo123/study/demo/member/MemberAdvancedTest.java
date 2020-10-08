package edu.pasudo123.study.demo.member;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import edu.pasudo123.study.demo.team.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.TestPropertySources;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;

import static edu.pasudo123.study.demo.member.QMember.member;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Member 엔티티 테스트 클래스는")
@ActiveProfiles("test")
@Transactional
public class MemberAdvancedTest {

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
    @DisplayName("튜플 대상이 하나일 때")
    public void oneTupleSelectTest() {
        List<String> names = queryFactory
                .select(member.username)
                .from(member)
                .fetch();

        assertThat(names).hasSize(4);
    }

    @Test
    @DisplayName("튜플 대상이 여러 개일때")
    public void multiTupleSelectTest() {

        /**
         * 튜플을 서비스 레이어나 컨트롤러 레이어까지 넘어가는 것은 좋은 설계가 아니다.
         * 나머지 계층으로 넘어가지 않도록 한다. 바깥계층으로 나가는 것은 Dto 객체로 변경해서 나가는 것을 권장한다.
         */

        final List<Tuple> tuples = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();

        assertThat(tuples).hasSize(4);

        for(Tuple tuple : tuples) {
            System.out.println(tuple.get(member.username) + " : " + tuple.get(member.age));
        }
    }

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
    }
}
