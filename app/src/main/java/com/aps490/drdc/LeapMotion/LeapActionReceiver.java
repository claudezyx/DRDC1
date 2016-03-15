package com.aps490.drdc.LeapMotion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.aps490.drdc.customlayouts.DrawingView;
import com.aps490.drdc.prototype.Constants;
import com.aps490.drdc.prototype.R;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by JollyRancher on 16-02-21.
 */
public class LeapActionReceiver extends BroadcastReceiver {

    private int screenWidth;

    private int screenHeight;

    private final int leapXNormalizer = 350;

    private final int leapBoundXMin = 100;

    private final int leapBoundXMax = 650;

    private final int leapBoundYMin = 100;

    private final int leapBoundYMax = 450;

    private Context mCurrentContext;

    private Boolean hit = false;

    // The root activity view to traverse for a screen tap hit
    private View rootView;

    public LeapActionReceiver(Context context, View view) {
        super();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
        rootView = view;
        Log.d(Constants.TAG, "Android Screen: " + screenWidth + " x " + screenHeight);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        mCurrentContext = context;
        String action = intent.getAction();

        switch (action) {
            case Constants.LEAP_PAYLOAD_TO_PROCESS:

                processPayLoad(context, intent);

                break;
            default:
                break;
        }
    }

    private void processPayLoad(Context context, Intent intent) {

        String payload = intent.getStringExtra("payload");

        try {
            JSONObject frame = new JSONObject(payload);

            /**
             * This is where we parse any pointables. If one finger (Index) is deteced, parse it's
             * location coordinate and send it to the DrawerView in the activity to draw on screen.
             *
             * If two fingers were detected (Thumb and Index) then it is a click; parse the coordinate
             * from the index finger, and notify theLeapTapEventReceivers (via broadcast) that a
             * click was performed.
             */

            try {
                // Pointables
                JSONArray pointables = frame.getJSONArray("pointables");

                if (pointables.length() != 0) {

                    Double x = 0.0;
                    Double y = 0.0;

                    switch (pointables.length()) {
                        case 1:
                            x = pointables.getJSONObject(0).getJSONArray("tipPosition").getDouble(0);
                            y = pointables.getJSONObject(0).getJSONArray("tipPosition").getDouble(1);

                            break;
                        case 2:
                            x = pointables.getJSONObject(Constants.FINGER_INDEX).getJSONArray("tipPosition").getDouble(0);
                            y = pointables.getJSONObject(Constants.FINGER_INDEX).getJSONArray("tipPosition").getDouble(1);
                            break;
                        default:
                            break;
                    }

                    Point norm_point = normalizePointToScreenDevice(x, y);

                    if (norm_point != null) {

                        if (pointables.length() > 1 && !hit) {
                            View hitView = findHitView(rootView, norm_point);

                            if (hitView != null) {

                                // Send relevant view ID back to main thread to handle
                                Intent tappedViewIntent = new Intent();
                                tappedViewIntent.setAction(Constants.LEAP_TAP_RELEVANT_VIEW);
                                tappedViewIntent.addCategory(Intent.CATEGORY_DEFAULT);
                                tappedViewIntent.putExtra("viewID", hitView.getId());
                                tappedViewIntent.putExtra("hitX", norm_point.x);
                                tappedViewIntent.putExtra("hitY", norm_point.y);
                                context.sendBroadcast(tappedViewIntent);
                                hit = true;
                            }
                        } else {
                            View surface = rootView.findViewById(R.id.surfaceView);
                            if (surface != null) {
                                MotionEvent e = MotionEvent.obtain(SystemClock.uptimeMillis(),
                                        SystemClock.uptimeMillis(),
                                        MotionEvent.ACTION_HOVER_MOVE,
                                        norm_point.x, norm_point.y, 0);
                                e.setSource(0x00002002);
                                surface.dispatchGenericMotionEvent(e);
                                hit = false;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(Constants.TAG, "Error in Pointables: ", e);
            }

            /**
             * Parse any gestures and
             */

            // Gestures
            if (frame.getJSONArray("gestures").length() != 0) {
                JSONObject gesture = frame.getJSONArray("gestures").getJSONObject(0);

                String gestureType = gesture.getString("type");

                switch (gestureType){
                    case "swipe":
                        try {
                            String state = gesture.getString("state");
                            if (state.equals("stop")) {
                                Double swipeDirectionX = gesture.getJSONArray("direction").getDouble(0);
                                if (swipeDirectionX > 0) {
                                    Log.i(Constants.TAG, "You swiped to the right!");
                                } else if (swipeDirectionX <= 0) {
                                    Log.i(Constants.TAG, "You swiped to the left!");
                                }
                            }
                        } catch (Exception e) {
                            Log.i(Constants.TAG, "Error in Gesture (Swipe): ", e);
                        }
                        break;
                    case "screenTap":

                        /**
                         * I am no longer looking to rely on screenTap as an indication of a click.
                         * When a tap is registered on the leap, the difference in coordinates
                         * compared to the last known pointable coordinate is too different, so the
                         * click ends up being elsewhere on the screen/element.
                         *
                         * See The pointables section above for the rest of description.
                         */
/*                        try {
                            Double tapX = gesture.getJSONArray("position").getDouble(0);
                            Double tapY = gesture.getJSONArray("position").getDouble(1);

                            Point norm_tap_coord = normalizePointToScreenDevice(tapX, tapY);

                            if (norm_tap_coord != null) {
                                Log.i(Constants.TAG, "Screen Tap at " + norm_tap_coord.x + " " + norm_tap_coord.y);

                                View hitView = findHitView(rootView, norm_tap_coord);

                                if (hitView != null) {

                                    // Send relevant view ID back to main thread to handle
                                    Intent tappedViewIntent = new Intent();
                                    tappedViewIntent.setAction(Constants.LEAP_TAP_RELEVANT_VIEW);
                                    tappedViewIntent.addCategory(Intent.CATEGORY_DEFAULT);
                                    tappedViewIntent.putExtra("viewID", hitView.getId());
                                    tappedViewIntent.putExtra("hitX", norm_tap_coord.x);
                                    tappedViewIntent.putExtra("hitY", norm_tap_coord.y);
                                    context.sendBroadcast(tappedViewIntent);

                                }
                            }
                        } catch (Exception e) {
                            Log.e(Constants.TAG, "Error in Gestures (Tap): ", e);
                        }
*/

                        break;
                    default:
                        //Log.i(Constants.TAG, "Gesture not recognized yet!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Try and find the first inner child view that was hit, starting from the
     * root view of the activity.
     * @param rV root view of activity
     * @param tap Point where screen was tapped (simulation)
     * @return view that the screen tap hit. Null if none.
     */
    private View findHitView(View rV, Point tap) {
        View hit = null;
        Rect hitRect;
        for(int i=0; i<( (ViewGroup) rV).getChildCount(); ++i) {
            hitRect = new Rect();
            View nextChild = ( (ViewGroup) rV).getChildAt(i);

            try {
                if ( ( (ViewGroup) nextChild).getChildCount() > 0) {
                    hit = findHitView(nextChild, tap);
                    if (hit != null) {
                        return hit;
                    }
                }
            } catch (ClassCastException e) {
               // Log.e(Constants.ERROR, e.toString());
            }

            nextChild.getHitRect(hitRect);

            if ( !(nextChild instanceof DrawingView) ) {
                if (hitRect.contains(tap.x, tap.y)) {
                    hit = nextChild;
                    //Log.i(Constants.TAG, "Child hit rect:" + hitRect.flattenToString() + " Class: " + nextChild.getClass());

                    return hit;
                }
            }
        }
        return hit;
    }

    /**
     * Normal the leap motion coordinates to the android device's pixel coordinates.
     * Also enforces a bounding box; if the raw coordinates are not within
     * leapBoundXMin < leapBoundXMax and
     * leapBoundYMin < LeapBoundYMax
     * then do not attempt to normalize the coordinate.
     *
     * @param xRaw raw x coordinate from Leap motion
     * @param yRaw raw y coordinate from Leap motion
     * @return Point with normalized x and y coordinates, or null if outside bounding box
     */
    private Point normalizePointToScreenDevice(Double xRaw, Double yRaw) {
        
        // Leap coordinates start from ~(-350, 0, X)
        // so no need to normalize Y coordinate
        Double x = xRaw + leapXNormalizer;
        Double y = yRaw;

        if ( !(leapBoundXMin <= x) || !(x <= leapBoundXMax) ) {
            return null;
        }

        if ( !(leapBoundYMin <= y) || !(y <= leapBoundYMax) ) {
            return null;
        }

        // The pointer is within our bounding box, continue processing
        Point new_point = new Point();
        Double x_norm = x - leapBoundXMin;
        Double y_norm = y - leapBoundYMin;

        Double x_norm_factor = x_norm / (leapBoundXMax - leapBoundXMin);
        Double y_norm_factor = ((leapBoundYMax - leapBoundYMin) - y_norm) / (leapBoundYMax - leapBoundYMin);
        //Double y_norm_factor_flip = 1.0 - y_norm_factor;

        new_point.x = (int) (screenWidth * x_norm_factor);
        new_point.y = (int) (screenHeight * y_norm_factor);

        return new_point;

    }
}