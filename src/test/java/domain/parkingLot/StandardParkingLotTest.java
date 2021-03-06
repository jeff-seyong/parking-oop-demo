package domain.parkingLot;

import domain.car.Bus;
import domain.car.Car;
import domain.car.GeneralCar;
import domain.car.LightCar;
import domain.parkingBox.map.StandardMap;
import domain.parkingLot.dto.CarParkingInfo;
import domain.parkingLot.dto.CarParkingInfoBuilder;
import domain.parkingLot.dto.ParkingLotMapInfo;
import domain.parkingLot.dto.ParkingReceipt;
import domain.policy.ParkingCostPolicy;
import domain.policy.StandardPolicy;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import utils.Location;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StandardParkingLotTest {

    private ParkingLot parkingLot;
    private Car bus;
    private Car generalCar;
    private Car lightCar;

    @BeforeEach
    void init() {
        int floor = 10;
        int no = 100;
        ParkingCostPolicy policy = new StandardPolicy(500);
        ParkingLotMapInfo map = new ParkingLotMapInfo(floor, no, new StandardMap());
        parkingLot = new StandardParkingLot(map, policy);

        bus = new Bus("100버 1000");
        generalCar = new GeneralCar("200일 2000");
        lightCar = new LightCar("300경 3000");

        /**
         * [100경 9901]-(10F,8) / [200경 9902]-(10F,16) / [300경 9903]-(10F,24)
         */
        for (int i = 1; i <= 3; i++) {
            CarParkingInfo carParkingInfo = new CarParkingInfo("ForTest", new LightCar(i + "00경 990" + i));
            carParkingInfo.setLocation(10, i * 8);
            parkingLot.park(carParkingInfo);
        }
    }


    @Test
    @Order(1)
    @DisplayName("StandardParkingLot을 생성한다")
    void createParkingLot() {
        // given
        int floor = 10;
        int no = 100;
        ParkingCostPolicy policy = new StandardPolicy(500);
        ParkingLotMapInfo map = new ParkingLotMapInfo(floor, no, new StandardMap());

        // when
        ParkingLot standardParkingLot = new StandardParkingLot(map, policy);

        // then
        assertNotNull(standardParkingLot);
    }

    @Test
    @Order(2)
    @DisplayName("주차장 만들 때 잘못된 층,칸,정책이 주어진 경우에 에러를 발생시킨다")
    void wrongFloorOrNo() {
        // given
        int wrongFloor = 0;
        int no = 100;
        int floor = 10;
        int wrongNo = -10;
        ParkingLotMapInfo mapWrongFloor = new ParkingLotMapInfo(wrongFloor, no, new StandardMap());
        ParkingLotMapInfo mapWrongNo = new ParkingLotMapInfo(floor, wrongNo, new StandardMap());
        ParkingLotMapInfo map = new ParkingLotMapInfo(floor, no, new StandardMap());
        ParkingCostPolicy policy = new StandardPolicy(500);
        ParkingCostPolicy wrongPolicy = null;

        // when
        assertThrows(IllegalArgumentException.class, () -> new StandardParkingLot(mapWrongFloor, policy));
        assertThrows(IllegalArgumentException.class, () -> new StandardParkingLot(mapWrongNo, policy));
        assertThrows(IllegalArgumentException.class, () -> new StandardParkingLot(map, wrongPolicy));
    }

    @Test
    @DisplayName("차량이 진입했을 때 차량의 정보를 잘 저장한다")
    @Order(3)
    void enterTheCar() {
        // when
        CarParkingInfo enterBus = parkingLot.enter(bus);
        CarParkingInfo enterGen = parkingLot.enter(generalCar);
        CarParkingInfo enterLig = parkingLot.enter(lightCar);

        // then
        assertEquals(bus.getCarNum(), enterBus.getCar().getCarNum());
        assertNotNull(enterBus.getEnterTime());
        log.info("parkingName = {}", enterBus.getParkingLotName());
        log.info("carNum = {}", enterBus.getCar().getCarNum());
        log.info("enterTime = {}", enterBus.getEnterTime());

        assertEquals(generalCar.getCarNum(), enterGen.getCar().getCarNum());
        assertNotNull(enterGen.getEnterTime());
        log.info("parkingName = {}", enterBus.getParkingLotName());
        log.info("carNum = {}", enterBus.getCar().getCarNum());
        log.info("enterTime = {}", enterBus.getEnterTime());

        assertEquals(lightCar.getCarNum(), enterLig.getCar().getCarNum());
        assertNotNull(enterLig.getEnterTime());
        log.info("parkingName = {}", enterBus.getParkingLotName());
        log.info("carNum = {}", enterBus.getCar().getCarNum());
        log.info("enterTime = {}", enterBus.getEnterTime());
    }

    @Test
    @Order(4)
    @DisplayName("차량 이름 정보가 잘못된 차량이 진입했을 때 에러를 발생시킨다")
    void enterWrongCar() {
        // given
        // TODO: Car 의 테스트로 이동
        Bus nameIsNullCar = new Bus(null);
        Bus emptyNameCar = new Bus("");

        // then
        assertThrows(IllegalArgumentException.class, () -> parkingLot.enter(null));
        assertThrows(IllegalArgumentException.class, () -> parkingLot.enter(nameIsNullCar));
        assertThrows(IllegalArgumentException.class, () -> parkingLot.enter(emptyNameCar));
    }

    @Test
    @Order(5)
    @DisplayName("주차칸에 차량이 주차한다")
    void park() {
        // given
        /**
         * StandardParkingLot
         * - floor = 10 / no = 100
         * - 각 층 마지막 번호는 버스 전용칸
         * - 각 층 1~99번 중 짝수 번호는 일반차 전용
         * - 각 층 1~99번 중 홀수 번호는 경차 전용
         */
        CarParkingInfo enteredBus = parkingLot.enter(bus);
        enteredBus.setLocation(1, 100);
        CarParkingInfo enteredGen = parkingLot.enter(generalCar);
        enteredGen.setLocation(2, 2);
        CarParkingInfo enteredLig = parkingLot.enter(lightCar);
        enteredLig.setLocation(3, 1);

        // when
        parkingLot.park(enteredBus);
        parkingLot.park(enteredGen);
        parkingLot.park(enteredLig);

        // then
        parkingLot.show();
    }

    @Test
    @Order(6)
    @DisplayName("전용 칸에 주차 하지 않은 경우 에러를 발생시킨다")
    void diffTypeCarPark() {
        // given
        /**
         * StandardParkingLot
         * - floor = 10 / no = 100
         * - 각 층 마지막 번호는 버스 전용칸
         * - 각 층 1~99번 중 짝수 번호는 일반차 전용
         * - 각 층 1~99번 중 홀수 번호는 경차 전용
         * (단, 경차는 일반차 전용칸에도 주차 가능)
         */
        CarParkingInfo busInGeneralZone = parkingLot.enter(bus);
        busInGeneralZone.setLocation(3, 10);
        CarParkingInfo genInLightZone = parkingLot.enter(generalCar);
        genInLightZone.setLocation(3, 33);
        CarParkingInfo ligInBusZone = parkingLot.enter(lightCar);
        ligInBusZone.setLocation(3, 100); // 버스 전용칸인 경우

        // when
        assertThrows(RuntimeException.class, () -> parkingLot.park(busInGeneralZone));
        assertThrows(RuntimeException.class, () -> parkingLot.park(genInLightZone));
        assertThrows(RuntimeException.class, () -> parkingLot.park(ligInBusZone));
    }

    @Test
    @Order(7)
    @DisplayName("단, 경차는 일반차 전용칸에 주차를 할 수 있다")
    void lightCarParkInGeneralZone() {
        // given
        CarParkingInfo ligInGeneralZone = parkingLot.enter(lightCar);
        ligInGeneralZone.setLocation(3, 22); // 일반차 전용칸인 경우

        // when
        parkingLot.park(ligInGeneralZone);

        // then
        parkingLot.show();
    }

    @Test
    @Order(8)
    @DisplayName("주차칸에 이미 차량이 있는 경우 에러를 발생시킨다")
    void alreadyExistCarInParkingBox() {
        // given
        CarParkingInfo busFirst = parkingLot.enter(bus);
        CarParkingInfo busSecond = parkingLot.enter(bus);
        busFirst.setLocation(10, 100);
        busSecond.setLocation(10, 100);

        CarParkingInfo genFirst = parkingLot.enter(generalCar);
        CarParkingInfo genSecond = parkingLot.enter(generalCar);
        genFirst.setLocation(3, 20);
        genSecond.setLocation(3, 20);

        CarParkingInfo ligFirst = parkingLot.enter(lightCar);
        CarParkingInfo ligSecond = parkingLot.enter(lightCar);
        ligFirst.setLocation(3, 33);
        ligSecond.setLocation(3, 33);

        // when
        parkingLot.park(busFirst);
        parkingLot.park(genFirst);
        parkingLot.park(ligFirst);

        // then
        assertThrows(RuntimeException.class, () -> parkingLot.park(busSecond));
        assertThrows(RuntimeException.class, () -> parkingLot.park(genSecond));
        assertThrows(RuntimeException.class, () -> parkingLot.park(ligSecond));
    }

    @Test
    @Order(9)
    @DisplayName("주차칸에서 차량이 출차 한다")
    void moveOut() {
        // given
        CarParkingInfo car = parkingLot.enter(generalCar);
        car.setLocation(10, 10);
        parkingLot.park(car);
        parkingLot.show();

        // when
        parkingLot.moveOut(car);

        // then
        parkingLot.show();
    }

    @Test
    @Order(10)
    @DisplayName("중복 출차 시(차량 출차 시 주차칸에 차량이 없는 경우) 에러를 발생 시킨다")
    void doubleMoveOut() {
        // given
        CarParkingInfo car = parkingLot.enter(generalCar);
        car.setLocation(10, 10);
        parkingLot.show();

        // when
        assertThrows(RuntimeException.class, () -> parkingLot.moveOut(car));
    }

    @Test
    @Order(11)
    @DisplayName("주차된 차량을 조회한다")
    void find() {
        // given
        /**
         * [100경 9901]-(10F,8) / [200경 9902]-(10F,16) / [300경 9903]-(10F,24)
         */
        LightCar lightCar = new LightCar("100경 9901");
        parkingLot.show();

        // when
        CarParkingInfo carParkingInfo = parkingLot.find(lightCar);

        // then
        assertNotNull(carParkingInfo);
        assertEquals("100경 9901", carParkingInfo.getCar().getCarNum());
        assertEquals(10, carParkingInfo.getFloor());
        assertEquals(8, carParkingInfo.getParkingBoxNo());
    }

    @Test
    @Order(12)
    @DisplayName("조회되는 차량이 없는 경우 null을 반환한다")
    void findButNotExist() {
        // given
        /**
         * [100경 9901]-(10F,8) / [200경 9902]-(10F,16) / [300경 9903]-(10F,24)
         */
        LightCar notExistCar = new LightCar("999경 1234");

        // when
        CarParkingInfo carParkingInfo = parkingLot.find(lightCar);

        // then
        assertNull(carParkingInfo);
    }

    @Test
    @Order(13)
    @DisplayName("차량이 주차장을 나가려고 할 때 차량을 조회하여 나가는 시간을 기록한다")
    void leave() {
        // given
        LightCar lightCar = new LightCar("100경 9901");

        // when
        CarParkingInfo leftCar = parkingLot.leave(lightCar);

        // then
        assertNotNull(leftCar);
        assertNotNull(leftCar.getLeaveTime());
        System.out.println("leftCar.getCar().getCarNum() = " + leftCar.getCar().getCarNum());
        System.out.println("leftCar.getLeaveTime() = " + leftCar.getLeaveTime());
    }

    @Test
    @Order(14)
    @DisplayName("주차 요금을 계산한다")
    void calculate() {
        // given
        /**
         * policy : 10분에 500원
         */
        LightCar lightCar = new LightCar("400경 9904");
        LocalDateTime enterTime = LocalDateTime.of(LocalDate.of(2022, 5, 15), LocalTime.of(13, 0));
        LocalDateTime leaveTime = LocalDateTime.of(LocalDate.of(2022, 5, 15), LocalTime.of(15, 24));
        CarParkingInfo parking120Min = CarParkingInfoBuilder.of("ForTest", Location.of(1, 1), lightCar, enterTime, leaveTime);

        // when
        ParkingReceipt receipt = parkingLot.calculate(parking120Min);

        // then
        assertNotNull(receipt);
        assertEquals(7200, receipt.getCost());
    }
}