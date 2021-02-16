package cinema;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;

@Entity
@Table(name="Cinema_table")
public class Cinema {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String cinemaNo;
    private String time;
    private String status;
    private Long reservationNo;

    @PostPersist
    public void onPostPersist(){
        ReservAccepted reservAccepted = new ReservAccepted();
        BeanUtils.copyProperties(this, reservAccepted);
        reservAccepted.publishAfterCommit();


        ReserveCanceled reserveCanceled = new ReserveCanceled();
        BeanUtils.copyProperties(this, reserveCanceled);
        reserveCanceled.publishAfterCommit();


    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getCinemaNo() {
        return cinemaNo;
    }

    public void setCinemaNo(String cinemaNo) {
        this.cinemaNo = cinemaNo;
    }
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public Long getReservationNo() {
        return reservationNo;
    }

    public void setReservationNo(Long reservationNo) {
        this.reservationNo = reservationNo;
    }




}
