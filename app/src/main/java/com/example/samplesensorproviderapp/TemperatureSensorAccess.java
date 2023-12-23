package com.example.samplesensorproviderapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class TemperatureSensorAccess implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor mTemperature;
    private TextView textViewTemperature;

    private Mqtt5BlockingClient mqttClient;
    private String mqttBrokerURI = "3.220.83.84";

    public TemperatureSensorAccess(SensorManager sm, TextView tv) {
        sensorManager = sm;
        textViewTemperature = tv;

        mqttClient = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(mqttBrokerURI)
                .buildBlocking();


        mTemperature = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);


        if (mTemperature != null) {
            sensorManager.registerListener(this, mTemperature, SensorManager.SENSOR_DELAY_NORMAL);
        } else {

            textViewTemperature.setText("Temperature sensor not available on this device");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float temperatureValue = event.values[0];
        textViewTemperature.setText(temperatureValue + " °C");

        publishToMQTT("temperature", String.valueOf(temperatureValue));
    }

    private void publishToMQTT(String topic, String message) {
        try {
            mqttClient.connect();
            mqttClient.publishWith()
                    .topic(topic)
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .payload(message.getBytes(StandardCharsets.UTF_8))
                    .send();
            mqttClient.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unregisterSensorListener() {
        sensorManager.unregisterListener(this);
    }
}