package org.jftclient.tree;

import com.sun.javafx.scene.control.skin.VirtualFlow;

import javafx.scene.control.IndexedCell;

public class CustomVirtualFlow<T extends IndexedCell> extends VirtualFlow<T> {

    @Override
    public double getPosition() {
        double position = super.getPosition();
        if (position == 1.0d) {
            return 0.99999999999;
        }
        return super.getPosition(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setPosition(double newPosition) {
        if (newPosition == 1.0d) {
            newPosition = 0.99999999999;
        }
        super.setPosition(newPosition); //To change body of generated methods, choose Tools | Templates.
    }

}
