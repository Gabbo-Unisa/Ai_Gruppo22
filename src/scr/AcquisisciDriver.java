package scr;

import javax.swing.*;
import java.io.*;
import java.sql.Struct;
import java.util.Locale;

public class AcquisisciDriver extends Controller {
    private PrintWriter csvWriter;

    /* Controlli dell'utente */
    private volatile boolean accel;
    private volatile boolean brake;
    private volatile boolean steerLeft;
    private volatile boolean steerRight;

    /* Costanti di cambio marcia */
    final int[] gearUp = { 11000, 14000, 18000, 18000, 18000, 18000, 0 };
    final int[] gearDown = { 0, 5000, 5000, 12000, 12000, 12000, 12000 };

    /* Costanti */
    final int stuckTime = 25;
    final float stuckAngle = (float) 0.523598775; // PI/6

    /* Costanti di accelerazione e di frenata */
    final float maxSpeedDist = 70;
    final float maxSpeed = 150;
    final float sin5 = (float) 0.08716;
    final float cos5 = (float) 0.99619;

    /* Costanti di sterzata */
    final float steerLock = (float) 0.785398;
    final float steerSensitivityOffset = (float) 80.0;
    final float wheelSensitivityCoeff = 1;

    /* Costanti del filtro ABS */
    final float wheelRadius[] = { (float) 0.3179, (float) 0.3179, (float) 0.3276, (float) 0.3276 };
    final float absSlip = (float) 2.0;
    final float absRange = (float) 3.0;
    final float absMinSpeed = (float) 3.0;

    /* Costanti da stringere */
    final float clutchMax = (float) 0.5;
    final float clutchDelta = (float) 0.05;
    final float clutchRange = (float) 0.82;
    final float clutchDeltaTime = (float) 0.02;
    final float clutchDeltaRaced = 10;
    final float clutchDec = (float) 0.01;
    final float clutchMaxModifier = (float) 1.3;
    final float clutchMaxTime = (float) 1.5;

    private int stuck = 0;

    // current clutch
    private float clutch = 0;

    public AcquisisciDriver() {
        SwingUtilities.invokeLater(() -> new ContinuousCharReaderUI(this));
        try {
            csvWriter = new PrintWriter(new BufferedOutputStream(new FileOutputStream("F1_primi5giri.csv")));
            csvWriter.append("angle;curLapTime;damage;distRaced;distFromStart;speedX;speedY;track_0;track_1;track_2;track_3;track_4;track_5;track_6;track_7;track_8;track_9;track_10;track_11;track_12;track_13;track_14;track_15;track_16;track_17;track_18;trackPos;class\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void reset() {
        System.out.println("Restarting the race!");

    }

    public void shutdown() {
        System.out.println("Bye bye!");
        csvWriter.close();
    }

    private int getGear(SensorModel sensors) {
        int gear = sensors.getGear();
        double rpm = sensors.getRPM();

        // Se la marcia è 0 (N) o -1 (R) restituisce semplicemente 1
        if (gear < 1)
            return 1;

        // Se il valore di RPM dell'auto è maggiore di quello suggerito
        // sale di marcia rispetto a quella attuale
        if (gear < 7 && rpm >= gearUp[gear - 1])
            return gear + 1;
        else

            // Se il valore di RPM dell'auto è inferiore a quello suggerito
            // scala la marcia rispetto a quella attuale
            if (gear > 1 && rpm <= gearDown[gear - 1])
                return gear - 1;
            else // Altrimenti mantenere l'attuale
                return gear;
    }

    private float getSteer(SensorModel sensors) {
        /** L'angolo di sterzata viene calcolato correggendo l'angolo effettivo della vettura
         * rispetto all'asse della pista [sensors.getAngle()] e regolando la posizione della vettura
         * rispetto al centro della pista [sensors.getTrackPos()*0,5].
         */
        float targetAngle = (float) (sensors.getAngleToTrackAxis() - sensors.getTrackPosition() * 0.5);
        // ad alta velocità ridurre il comando di sterzata per evitare di perdere il controllo
        if (sensors.getSpeed() > steerSensitivityOffset)
            return (float) (targetAngle
                    / (steerLock * (sensors.getSpeed() - steerSensitivityOffset) * wheelSensitivityCoeff));
        else
            return (targetAngle) / steerLock;
    }

    private float getAccel(SensorModel sensors) {
        // controlla se l'auto è fuori dalla carreggiata
        if (sensors.getTrackPosition() > -1 && sensors.getTrackPosition() < 1) {
            // lettura del sensore a +5 gradi rispetto all'asse dell'automobile
            float rxSensor = (float) sensors.getTrackEdgeSensors()[10];
            // lettura del sensore parallelo all'asse della vettura
            float sensorsensor = (float) sensors.getTrackEdgeSensors()[9];
            // lettura del sensore a -5 gradi rispetto all'asse dell'automobile
            float sxSensor = (float) sensors.getTrackEdgeSensors()[8];

            float targetSpeed;

            // Se la pista è rettilinea e abbastanza lontana da una curva, quindi va alla massima velocità
            if (sensorsensor > maxSpeedDist || (sensorsensor >= rxSensor && sensorsensor >= sxSensor))
                targetSpeed = maxSpeed;
            else {
                // In prossimità di una curva a destra
                if (rxSensor > sxSensor) {

                    // Calcolo dell'"angolo" di sterzata
                    float h = sensorsensor * sin5;
                    float b = rxSensor - sensorsensor * cos5;
                    float sinAngle = b * b / (h * h + b * b);

                    // Set della velocità in base alla curva
                    targetSpeed = maxSpeed * (sensorsensor * sinAngle / maxSpeedDist);
                }
                // In prossimità di una curva a sinistra
                else {
                    // Calcolo dell'"angolo" di sterzata
                    float h = sensorsensor * sin5;
                    float b = sxSensor - sensorsensor * cos5;
                    float sinAngle = b * b / (h * h + b * b);

                    // eSet della velocità in base alla curva
                    targetSpeed = maxSpeed * (sensorsensor * sinAngle / maxSpeedDist);
                }
            }

            /**
             * Il comando di accelerazione/frenata viene scalato in modo esponenziale rispetto
             * alla differenza tra velocità target e quella attuale
             */
            return (float) (2 / (1 + Math.exp(sensors.getSpeed() - targetSpeed)) - 1);
        } else
            // Quando si esce dalla carreggiata restituisce un comando di accelerazione moderata
            return (float) 0.3;
    }



    private float filterABS(SensorModel sensors, float brake) {
        // Converte la velocità in m/s
        float speed = (float) (sensors.getSpeed() / 3.6);

        // Quando la velocità è inferiore alla velocità minima per l'abs non interviene in caso di frenata
        if (speed < absMinSpeed)
            return brake;

        // Calcola la velocità delle ruote in m/s
        float slip = 0.0f;
        for (int i = 0; i < 4; i++) {
            slip += sensors.getWheelSpinVelocity()[i] * wheelRadius[i];
        }

        // Lo slittamento è la differenza tra la velocità effettiva dell'auto e la velocità media delle ruote
        slip = speed - slip / 4.0f;

        // Quando lo slittamento è troppo elevato, si applica l'ABS
        if (slip > absSlip) {
            brake = brake - (slip - absSlip) / absRange;
        }

        // Controlla che il freno non sia negativo, altrimenti lo imposta a zero
        if (brake < 0)
            return 0;
        else
            return brake;
    }

    float clutching(SensorModel sensors, float clutch) {

        float maxClutch = clutchMax;

        // Controlla se la situazione attuale è l'inizio della gara
        if (sensors.getCurrentLapTime() < clutchDeltaTime && getStage() == Stage.RACE
                && sensors.getDistanceRaced() < clutchDeltaRaced)
            clutch = maxClutch;

        // Regolare il valore attuale della frizione
        if (clutch > 0) {
            double delta = clutchDelta;
            if (sensors.getGear() < 2) {

                // Applicare un'uscita più forte della frizione quando la marcia è una e la corsa è appena iniziata.
                delta /= 2;
                maxClutch *= clutchMaxModifier;
                if (sensors.getCurrentLapTime() < clutchMaxTime)
                    clutch = maxClutch;
            }

            // Controllare che la frizione non sia più grande dei valori massimi
            clutch = Math.min(maxClutch, clutch);

            // Se la frizione non è al massimo valore, diminuisce abbastanza rapidamente
            if (clutch != maxClutch) {
                clutch -= delta;
                clutch = Math.max((float) 0.0, clutch);
            }
            // Se la frizione è al valore massimo, diminuirla molto lentamente.
            else
                clutch -= clutchDec;
        }
        return clutch;
    }

    public float[] initAngles() {

        float[] angles = new float[19];

        /*
         * set angles as
         * {-90,-75,-60,-45,-30,-20,-15,-10,-5,0,5,10,15,20,30,45,60,75,90}
         */
        for (int i = 0; i < 5; i++) {
            angles[i] = -90 + i * 15;
            angles[18 - i] = 90 - i * 15;
        }

        for (int i = 5; i < 9; i++) {
            angles[i] = -20 + (i - 5) * 5;
            angles[18 - i] = 20 - (i - 5) * 5;
        }
        angles[9] = 0;
        return angles;
    }

    public int determinaClasse(boolean accel, boolean brake, boolean steerLeft, boolean steerRight) {
        boolean steering = steerLeft || steerRight;

        if (!brake) {   // diamo priorità alla frenata
            if (accel) {
                if (!steering) {
                    return 0;   // accelerazione, nessuna sterzata
                } else if (steerLeft) {
                    return 1;   // accelerazione, con sterzata sx
                } else if (steerRight) {
                    return 2;   // accelerazione, con sterzata dx
                }
            } else if (!steering) {
                return 3;   // nessun comando
            } else if (steerLeft) {
                return 4;   // solo sterzata sx
            } else if (steerRight) {
                return 5;   // solo sterzata dx
            }
        } else if (!steering) {
            return 6;   // frenata, nessuna sterzata
        } else if (steerLeft) {
            return 7;   // frenata, con sterzata sx
        } else if (steerRight) {
            return 8;   // frenata, con sterzata dx
        }

        return -1;  // comando sconosciuto
    }

    public Action control(SensorModel sensors) {
        // Creazione di un nuovo oggetto Action
        Action action = new Action();

        // Calcolo della marcia
        int gear = getGear(sensors);

        // Calcolo dello sterzo
        double steering = 0;
        if (steerRight) {
            steering = -0.15;
        } else if (steerLeft) {
            steering = 0.15;
        }

        // Calcolo dell'accelerazione e frenata
        double accelerate = 0;
        double brk = 0;

        if (accel) {
            accelerate = 0.95;
        }

        if (brake) {
            if (sensors.getSpeed() < 1) {
                // se sono fermo, vado in retromarcia
                gear = -1;
                accelerate = 0.8;
            } else {
                // se sono in corsa, freno
                brk = 0.4;
            }
        }

        // Calcolo della frizione
        clutch = clutching(sensors, clutch);

        // Costruire una variabile CarControl
        action.accelerate = accelerate;
        action.brake = filterABS(sensors, (float) brk);
        action.steering = steering;
        action.clutch = clutch;
        action.gear = gear;

        // Scrivo i sensori e le azioni del giocatore su un file CSV
        csvWriter.format("%.6f;", sensors.getAngleToTrackAxis());
        csvWriter.format("%.6f;", sensors.getCurrentLapTime());
        csvWriter.format("%.6f;", sensors.getDamage());
        csvWriter.format("%.6f;", sensors.getDistanceRaced());
        csvWriter.format("%.6f;", sensors.getDistanceFromStartLine());
        csvWriter.format("%.6f;", sensors.getSpeed());
        csvWriter.format("%.6f;", sensors.getLateralSpeed());
        for (double edgeSensor : sensors.getTrackEdgeSensors()) {
            csvWriter.format("%.6f;", edgeSensor);
        }
        csvWriter.format("%.6f;", sensors.getTrackPosition());
        csvWriter.print(determinaClasse(accel, brake, steerLeft, steerRight));

        csvWriter.println("");

        csvWriter.flush();

        // Restituzione dell'azione calcolata
        return action;
    }

    public void setAccel(boolean accel) {
        this.accel = accel;
    }

    public void setBrake(boolean brake) {
        this.brake = brake;
    }

    public void setSteerLeft(boolean steerLeft) {
        this.steerLeft = steerLeft;
    }

    public void setSteerRight(boolean steerRight) {
        this.steerRight = steerRight;
    }

}
