package test.hibernate;

import javax.persistence.*;

public class All_classes {}

//Супер-класс людей
@MappedSuperclass
class Person {
    protected String full_name;
    private int age;
    private String gender;
    private long phone_number;
    private long passport_data;

    @Column(name = "person_full_name")
    public String getFullName() {
        return this.full_name;
    }
    public void setFullName(String valname) {
        this.full_name = valname;
    }

    @Column(name = "person_age")
    public int getAge() {
        return this.age;
    }
    public void setAge(int valage) {
        this.age = valage;
    }

    @Column(name = "person_gender")
    public String getGender() {
        return this.gender;
    }
    public void setGender(String valgend) {
        this.gender = valgend;
    }

    @Column(name = "person_phone_number")
    public long getNumber() {
        return this.phone_number;
    }
    public void setNumber(long valnumb) {
        this.phone_number = valnumb;
    }

    @Column(name = "person_passport_data")
    public long getPassport_data() {
        return this.passport_data;
    }
    public void setPassport_data(long valpass) {
        this.passport_data = valpass;
    }
}

//Класс Рабочих
@Entity
@Table(name="service_station_db.workers")
class Worker extends Person {
    private int worker_id;
    private int work_period = 0;
    private int specialization_id;
    private int salary;
    private int number_of_shifts;
    private String rest_day;

    @Id
    @Column(name = "worker_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int getWorker_id() {
        return this.worker_id;
    }

    public void setWorker_id(int worker_id) {
        this.worker_id = worker_id;
    }

    @Column(name = "worker_work_period")
    public int getWorkPeriod() {
        return this.work_period;
    }
    public void setWorkPeriod(int valper) {
        this.work_period = valper;
    }

    @Column(name = "worker_specialization_id")
    public int getSpecialization() {
        return this.specialization_id;
    }
    public void setSpecialization(int valspec) {
        this.specialization_id = valspec;
    }

    @Column(name = "worker_salary")
    public int getSalary() {
        return this.salary;
    }
    public void setSalary(int valsal) {
        this.salary = valsal;
    }

    @Column(name = "worker_number_of_shifts")
    public int getShifts() {
        return this.number_of_shifts;
    }
    public void setShifts(int valshift) {
        this.number_of_shifts = valshift;
    }

    @Column(name = "worker_rest_day")
    public String getRestDay() {
        return this.rest_day;
    }
    public void setRestDay(String valrest) {
        this.rest_day = valrest;
    }

    @Override
    public String toString() {
        return full_name + " (ID: " + worker_id+ ")";
    }
}

//Класс Клиентов
@Entity
@Table(name = "service_station_db.customers")
class Customer extends Person {
    private int customer_id;
    private int count_of_visiting = 1;
    private int id_car;
    private int discount_amount = 0;
    private String date_of_delivery;
    private String name;

    @Id
    @Column(name = "customer_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int getCustomer_id() {
        return this.customer_id;
    }

    public void setCustomer_id(int customer_id) {
        this.customer_id = customer_id;
    }

    @Column(name = "customer_count_of_visiting")
    public int getVisitingCount() {
        return this.count_of_visiting;
    }
    public void setVisitingCount(int valvisit) {
        count_of_visiting = valvisit;
    }

    @Column(name = "customer_discount_amount")
    public int getDiscount() {
        return this.discount_amount;
    }
    public void setDiscount(int valdisc) {
        discount_amount = valdisc;
    }

    @Column(name = "customer_date_of_delivery")
    public String getDeliveryDate() {
        return this.date_of_delivery;
    }
    public void setDeliveryDate(String valdelive) {
        date_of_delivery = valdelive;
    }
    @Override
    public String toString() {
        return full_name + " (ID: " + customer_id + ")";
    }
}

//Класс Автомобилей
@Entity
@Table(name = "service_station_db.cars")
class Car{
    private int car_id;
    private String car_name;
    private String body_type;
    private int year_of_release;
    private int engine_power;
    private int max_speed;
    private int mileage;
    private String sts_number;
    private String pts_number;
    private int car_cust_id;

    @Id
    @Column(name = "car_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int getCar_id() {
        return this.car_id;
    }

    public void setCar_id(int car_id) {
        this.car_id = car_id;
    }

    @Column(name = "car_name")
    public String getCarName() {
        return this.car_name;
    }
    public void setCarName(String valcar) {
        this.car_name = valcar;
    }

    @Column(name = "car_body_type")
    public String getBodyType() {
        return this.body_type;
    }
    public void setBodyType(String valbody) {
        this.body_type = valbody;
    }

    @Column(name = "car_year_of_release")
    public int getReleaseYear() {
        return this.year_of_release;
    }
    public void setReleaseYear(int valrel) {
        this.year_of_release = valrel;
    }

    @Column(name = "car_engine_power")
    public int getEnginePower() {
        return this.engine_power;
    }
    public void setEnginePower(int valpow) {
        this.engine_power = valpow;
    }

    @Column(name = "car_max_speed")
    public int getMaxSpeed() {
        return this.max_speed;
    }
    public void setMaxSpeed(int valspeed) {
        this.max_speed = valspeed;
    }

    @Column(name = "car_mileage")
    public int getMileage() {
        return this.mileage;
    }
    public void setMileage(int valmil) {
        this.mileage = valmil;
    }

    @Column(name = "car_sts_number")
    public String getSts() {
        return this.sts_number;
    }
    public void setSts(String valsts) {
        sts_number = valsts;
    }

    @Column(name = "car_pts_number")
    public String getPts() {
        return this.pts_number;
    }
    public void setPts(String valpts) {
        pts_number = valpts;
    }

    @Column(name = "car_customer_id")
    public int getCar_cust_id() {
        return this.car_cust_id;
    }

    public void setCar_cust_id(int car_cust_id) {
        this.car_cust_id = car_cust_id;
    }

    @Override
    public String toString() {
        return car_name + " (ID: " + car_id + ")"; // Формат отображения
    }
}

//Класс Оказанных услуг
@Entity
@Table(name = "service_station_db.rendered_services")
class Rendered_Service{
    private int service_id;
    private String service_name;
    private int service_worker;
    private int price_of_service;
    private String return_date;
    private int cust_id;

    @Id
    @Column(name = "service_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int getService_id() {
        return this.service_id;
    }

    public void setService_id(int service_id) {
        this.service_id = service_id;
    }

    @Column(name = "service_name")
    public String getServiceName() {
        return this.service_name;
    }
    public void setServiceName(String valservname) {
        this.service_name = valservname;
    }

    @Column(name = "service_worker_id")
    public int getService_worker() {
        return this.service_worker;
    }
    public void setService_worker(int service_worker) {
        this.service_worker = service_worker;
    }

    @Column(name = "service_price")
    public int getPrice() {
        return this.price_of_service;
    }
    public void setPrice(int valprice) {
        this.price_of_service = valprice;
    }

    @Column(name = "service_return_date")
    public String getReturnDate() {
        return this.return_date;
    }
    public void setReturnDate(String valreturn) {
        this.return_date = valreturn;
    }

    @Column(name = "service_customer_id")
    public int getCust_id() {
        return this.cust_id;
    }

    public void setCust_id(int cust_id) {
        this.cust_id = cust_id;
    }
}

//Класс Неисправностей
@Entity
@Table(name ="service_station_db.defects" )
class Defect{
    private int defid;
    private String name_of_defect;
    private int car_id;

    @Id
    @Column(name = "defect_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int getDefid() {
        return this.defid;
    }

    public void setDefid(int defid) {
        this.defid = defid;
    }

    @Column(name = "defect_name")
    public String getDefectName() {
        return this.name_of_defect;
    }
    public void setDefectName(String valdefname) {
        this.name_of_defect = valdefname;
    }

    @Column(name = "cars_defects_id")
    public int getCar_id() {
        return this.car_id;
    }

    public void setCar_id(int car_id) {
        this.car_id = car_id;
    }
};

//Класс Специальностей
@Entity
@Table(name = "service_station_db.specializations")
class Specialization {
    private int sid;
    private String specialization_name;

    @Id
    @Column(name = "specialization_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int getSid() {
        return this.sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }
    @Column(name = "specialization_name")
    public String getSpecialization_name() {
        return this.specialization_name;
    }

    public void setSpecialization_name(String specialization_name) {
        this.specialization_name = specialization_name;
    }

    @Override
    public String toString() {
        return specialization_name + " (ID: " + sid + ")"; // Формат отображения
    }
}