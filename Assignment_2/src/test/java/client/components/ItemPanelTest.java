package client.components;

import org.junit.Test;
import shared.Item;
import shared.ItemBuilder;

import javax.swing.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.UUID;


/**
 * ItemPanelTest
 *
 * @author Huw Jones
 * @since 20/04/2016
 */
public class ItemPanelTest {

    public static void main(String[] args){
        ItemPanelTest t = new ItemPanelTest();
        t.ItemPanelTest();
    }
    @Test
    public void ItemPanelTest(){
        if(System.getProperty("java.awt.headless") != null){
            return;
        }
        Calendar endTime = Calendar.getInstance();
        endTime.add(Calendar.DAY_OF_WEEK, 14);

        ItemBuilder itemBuilder = Item.createBuilder();
        itemBuilder
            .setID(UUID.randomUUID())
            .setUserID(UUID.randomUUID())
            .setTitle("Test Item")
            .setDescription("Brand new Test Item.<br /> Never opened before.<br /> Pristine condition.")
            .setReservePrice(new BigDecimal("2.50"))
            .setEndTime(new Timestamp(endTime.getTime().getTime()))
            .addKeyword("test");

        JFrame frame = new JFrame("ItemPanel Test");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        ItemPanel panel = new ItemPanel(itemBuilder.getItem());
        frame.add(panel);
        //frame.setMinimumSize(new Dimension(560, 212));
        frame.pack();
        frame.setVisible(true);
    }
}