package cinema;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="MyInfo_table")
public class MyInfo {

        @Id
        @GeneratedValue(strategy=GenerationType.AUTO)
        private Long id;
        private String cinemaNo;
        private String time;
        private String status;
        private Long reservationNo;


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
