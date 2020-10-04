package edu.pasudo123.study.demo.member;

import edu.pasudo123.study.demo.team.Team;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    private String username;
    private int age;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Team.class, cascade = CascadeType.ALL)
    @JoinColumn(name = "team_id", referencedColumnName = "id")
    @ToString.Exclude
    private Team team;

    @Builder
    public Member(final String username, final int age, final Team team){
        this.username = username;
        this.age = age;
        if(team != null) {
            changeTeam(team);
        }
    }

    public void changeTeam(final Team team) {
        this.team = team;
        team.getMembers().add(this);
    }
}
