package parkingBox;

import car.Bus;
import car.Car;
import car.LightCar;

public class BusParkingBox implements ParkingBox {
    private boolean carExist = false;

    @Override
    public void in(Car car) {
        if (!(car instanceof Bus) && !(car instanceof LightCar)) {
            throw new RuntimeException("버스 전용 칸입니다");
        }
        carExist = true;
    }

    @Override
    public void out() {
        carExist = false;
    }

    @Override
    public boolean status() {
        return this.carExist;
    }
}
