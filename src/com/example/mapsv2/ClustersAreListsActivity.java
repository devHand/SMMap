/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 OpenTable, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.example.mapsv2;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/*
 * Exercises for the reader
 * 
 * 1. Make the info window appear only after the map and marker have moved
 *    into place. Appearing beforehand is confusing and awkward. And maybe
 *    fade it in while you're at it.
 * 2. Limit the width of the info window in landscape mode so that it's not
 *    so overwhelmingly long.
 */
public class ClustersAreListsActivity extends FragmentActivity implements OnMarkerClickListener, OnCameraChangeListener {

	private GoogleMap map;
	private List<Marker> markers;
	private List<Cluster> clusters;
	private int clusterToleranceDIP = 8;
	private float mapZoom;
	// For Part 2
	private ListView listView;
	private View fullScreenOverlay, infoWindow;
	private int markerSize, defaultMargin;
	private CameraPosition previousCameraPosition;
	// For Part 3
	private GradientDrawable spotDrawable;
	private SpotlightAnimation spotAnimation;
	
	//For location
	Marker mMarker;
	LocationManager lm;
	double lat = -23.7003593445, lng = 133.8808898926;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clustersarelists);
        
     // location:
        lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

		boolean isNetwork = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		boolean isGPS = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

		if(isNetwork) {
			lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, listener);
			Location loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if(loc != null) {
				lat = loc.getLatitude();
			    lng = loc.getLongitude();
			}
		}
		
		if(isGPS) {
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, listener);
			Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if(loc != null) {
				lat = loc.getLatitude();
			    lng = loc.getLongitude();
			}
		}
		
		//markers:
        
        setUpMapIfNeeded();
        
        
        
        setupPart2();
        setupPart3();
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // location:
        lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

		boolean isNetwork = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		boolean isGPS = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
		
		if(isNetwork) {
			lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, listener);
			Location loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if(loc != null) {
				lat = loc.getLatitude();
			    lng = loc.getLongitude();
			}
		}
		
		if(isGPS) {
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, listener);
			Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if(loc != null) {
				lat = loc.getLatitude();
			    lng = loc.getLongitude();
			}
		}
		
		
    	
		//markers:
		setUpMapIfNeeded();
    }
    
    protected void onPause() {
    	super.onPause();
		lm.removeUpdates(listener);
    };
    
    //LocationListener
    LocationListener listener = new LocationListener() {
	    public void onLocationChanged(Location loc) {
	    	lat = loc.getLatitude();
	    	lng = loc.getLongitude();
	    	
	    	LatLng coordinate = new LatLng(lat,lng);
	    	
	    	
	    	if(mMarker != null)
	    		mMarker.remove();
	    	
	    	mMarker = map.addMarker(new MarkerOptions()
	    								.position(coordinate)
	    								.icon(BitmapDescriptorFactory.defaultMarker(360)));
	    	map.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 16));
	    }

	    public void onStatusChanged(String provider, int status, Bundle extras) {}
	    public void onProviderEnabled(String provider) {}
	    public void onProviderDisabled(String provider) {}
	};

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (map == null) {
            // Try to obtain the map from the SupportMapFragment.
            map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (map != null) {
                setUpMap();
                
                
            }
        }
    }

    private void setUpMap() {
        // Create some map markers to work with
        createMarkerDataSet();
        
        // Set listeners for marker events.  See the bottom of this class for their behavior
        map.setOnMarkerClickListener(this);
        // Listen for camera changes so we can detect zoom level change
        map.setOnCameraChangeListener(this);
        
        // Pan to see all markers in view.
        // Cannot zoom to bounds until the map has a size.
        final View mapView = getSupportFragmentManager().findFragmentById(R.id.map).getView();
        if (mapView.getViewTreeObserver().isAlive()) {
            mapView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                @SuppressWarnings("deprecation") // We use the new method when supported
                @SuppressLint("NewApi") // We check which build version we are using.
                @Override
                public void onGlobalLayout() {
                	LatLngBounds.Builder bld = new LatLngBounds.Builder();
                	for (Marker m : markers)
                		bld.include(m.getPosition());
                    LatLngBounds bounds = bld.build();
                    
                   if (Build.VERSION.SDK_INT < 17)//Build.VERSION_CODES.JELLY_BEAN)
                      mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    else
                     // mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                      mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    	
                    // Move the camera so that all markers are visible
                    // NOTE: This camera change will trigger a zoom level change which
                    // will cause a re-evaluation of the clusters
                    map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
                }
            });
        }
    }

    private void createMarkerDataSet() {
        // Creates a marker rainbow demonstrating how to create default marker icons of different
        // hues (colors).
    	if(mMarker != null)
        {
    		mMarker.remove();
        }
    	mMarker = map.addMarker(new MarkerOptions()
		.position(new LatLng(lat, lng)).title("center Marker "));
    	
        int numMarkersInRainbow = 12;
        markers = new ArrayList<Marker>(numMarkersInRainbow);
        for (int i = 0; i < numMarkersInRainbow; i++) {
            Marker m = map.addMarker(new MarkerOptions()
                    .position(new LatLng(
                    		lat + 0.01 * Math.sin(i * Math.PI / (numMarkersInRainbow - 1)),
                            lng - 0.01 * Math.cos(i * Math.PI / (numMarkersInRainbow - 1))))
                    .title("Marker " + i)
                    .icon(BitmapDescriptorFactory.defaultMarker(i * 360 / numMarkersInRainbow)));
            markers.add(m);
        }
    }

    /* -----------------------------------------------------------------------
     * Part 1: Clustering
     */
    private void computeClusters() {
    	if (clusters == null)
    		clusters = new LinkedList<Cluster>();
    	else
    		clusters.clear();
    	
    	Projection proj = map.getProjection();

    	for (Marker m : markers) {
    		// Project lat/lng into screen space, given map's current zoom, etc.
    		Point p = proj.toScreenLocation(m.getPosition());
    		// Find the first cluster near point p
    	 	Cluster cluster = null;
    	 	for (Cluster c : clusters) {
    			if (c.contains(p)) {
    				cluster = c;
    				break;
    			}
    		}
    		// Create a new Cluster if there were none nearby
    		if (cluster == null) {
    			cluster = new Cluster(p);
    			clusters.add(cluster);
    		}
    		cluster.add(m);
    	}
    }
    
	private void addClustersToMap() {
		// Remove any Markers already on the map
		map.clear();
        // Put a marker on the map for each Cluster, with appropriate icon
        for (Cluster c : clusters) {
			Marker m = c.get(0);
			int resId = (c.size() > 1 
					? R.drawable.marker_multi 
					: R.drawable.marker_single);
			Marker mapMarker = map.addMarker(new MarkerOptions()
				.position(m.getPosition())
				.title(m.getTitle())
				.icon(BitmapDescriptorFactory.fromResource(resId)));
			// FIXME: This is confusing, I think - the difference between Markers in the cluster
			// and Markers actually rendered on the map...
			c.mapMarkerId = mapMarker.getId();
		}
	}

    class Cluster {
    	Rect bounds;
    	List<Marker> markers = new LinkedList<Marker>();
    	String mapMarkerId;
    	public Cluster(Point p) {
    		// Delta should be in DIP units, so scale by screen density!
    		int delta = getClusterTolerance();
    		bounds = new Rect(p.x - delta, p.y - delta, p.x + delta, p.y + delta);
    	}
    	public boolean contains(Point p) {
    		return bounds.contains(p.x, p.y);
    	}
    	public void add(Marker m) {
    		markers.add(m);
    	}
    	public int size() {
     		return markers.size();
    	}
    	public Marker get(int i) {
    		return markers.get(i);
    	}
    }
    
    private int getClusterTolerance() {
		// Compute the maximum distance (in screen space pixels) between two
		// items on the map that will be displayed with one combined icon
		final float f = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				clusterToleranceDIP, getResources().getDisplayMetrics());
		return (int) (f + 0.5); // round up
    }
    
    /**
     * Re-evaluate the clustering of map markers if the zoom level has
     * changed enough.
     */
	@Override
	public void onCameraChange(CameraPosition position) {
		float zoom = position.zoom;
		if (Math.abs(zoom - mapZoom) > 0.6f) {
			mapZoom = zoom;
			// Re-compute the clustering given the current projection
			computeClusters();
			// Add the new clusters to the map
			addClustersToMap();
		}
	}

    /*
     * -- end Part 1
     */
    
    /* -----------------------------------------------------------------------
     * Part 2: The nougaty center
     * Clusters are lists
     * 
     */

	private void setupPart2() {
		fullScreenOverlay = findViewById(R.id.fullscreen_overlay);
		// Set the full screen overlay to be invisible but not GONE initially
		// because otherwise it's won't get width/height set in a layout pass
		fullScreenOverlay.setVisibility(View.INVISIBLE);
		fullScreenOverlay.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hideInfoWindow();
			}
		});
		
		listView = (ListView) findViewById(android.R.id.list);
		listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
				new LinkedList<String>()));
		// infowindow is the parent of the ListView and we'll position it to
		// appear to "point at" the selected map marker
		infoWindow = findViewById(R.id.info_window);
		// All our markers are the same width & height and are scaled exactly
		// for each density. YMMV.
		markerSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
						32, getResources().getDisplayMetrics());
		defaultMargin = getResources().getDimensionPixelOffset(R.dimen.activity_horizontal_margin);
	}
	
    @Override
    public boolean onMarkerClick(final Marker marker) {
    	// Find the cluster containing the Marker
    	Cluster cluster = null;
    	for (Cluster c : clusters) {
    		if (marker.getId().equals(c.mapMarkerId)) {
    			cluster = c;
    			break;
    		}
    	}

    	showInfoWindow(cluster);
    	// Consume the event, don't trigger the default behavior
        return true;
    }

    private void showInfoWindow(Cluster cluster) {
    	Marker m = cluster.get(0);
    	// Save the current camera so we can restore it later
    	previousCameraPosition = map.getCameraPosition();
    	
    	// Get the screen-space position of the Cluster
    	int yoffset = markerSize / 2;
		Projection proj = map.getProjection();
        Point p = proj.toScreenLocation(m.getPosition());
        int markerX = p.x;
        int markerY = p.y - yoffset;
		
		// Get the position where we'll ultimately show the marker & spotlight
		final Point newMarkerPos = getZoneCenter(m);
		
		// Animate the spotlight from the current marker position to its final position
		showSpotlight(markerX, markerY, newMarkerPos.x, newMarkerPos.y);
		
		// Position the selected marker in the top, right, bottom, or left zone
		final CameraUpdate camUpdate = calculateCameraChange(newMarkerPos.x, newMarkerPos.y, 
				markerX, markerY);
		map.animateCamera(camUpdate, 400, null);
		// Show the popup list next to the marker
		showInfo(newMarkerPos.x + markerSize/2, newMarkerPos.y + yoffset, cluster);
    }
    
    private Point getZoneCenter(final Marker marker) {
    	final int orientation = getResources().getConfiguration().orientation;	
        final int w = fullScreenOverlay.getWidth(); // this is full screen width
        final int h = fullScreenOverlay.getHeight(); // this is full screen height
        final int cx, cy;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
        	// Left Zone: Leftmost 1/4 of screen
          	cx = (w / 4) / 2;
           	cy = h / 2;
        } else {
          	// Top Zone: Upper 1/4 of screen
          	cx = w / 2;
          	cy = (h / 4) / 2;
        }
        return new Point(cx, cy);
    }
    
	private CameraUpdate calculateCameraChange(final int newX, final int newY,
			final int oldX, final int oldY) {
		// WARNING: This is broken when the map is tilted!
		Projection proj = map.getProjection();
		Point cameraPos = proj.toScreenLocation(map.getCameraPosition().target);
		int dx = newX - oldX;
		int dy = newY - oldY;
		cameraPos.x -= dx;
		cameraPos.y -= dy;
		return CameraUpdateFactory.newLatLng(proj.fromScreenLocation(cameraPos));
	}
	
	private void showInfo(final int x, int y, final Cluster cluster) {
		final int orientation = getResources().getConfiguration().orientation;	

    	// (re-)Load cluster data into the ListView
		@SuppressWarnings("unchecked")
		ArrayAdapter<String> adapter = (ArrayAdapter<String>) listView.getAdapter();
    	adapter.clear();
    	
    	for (Marker m : cluster.markers) {
    		adapter.add(m.getTitle());
    	}
    	adapter.notifyDataSetChanged();
    	
		// Reconfigure the layout params to position the info window on screen
		FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) infoWindow.getLayoutParams();
		
		if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			lp.topMargin = y;
			lp.leftMargin = defaultMargin;
			lp.rightMargin = defaultMargin;
			lp.width = LayoutParams.MATCH_PARENT;
			lp.height = LayoutParams.WRAP_CONTENT;
			lp.gravity = Gravity.LEFT | Gravity.TOP;
			infoWindow.setBackgroundResource(R.drawable.info_window_bg_up);
		} else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			lp.leftMargin = x + defaultMargin;
			lp.topMargin = defaultMargin;
			lp.bottomMargin = defaultMargin;
			lp.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
			lp.width = LayoutParams.WRAP_CONTENT;
			lp.height = LayoutParams.WRAP_CONTENT;
			infoWindow.setBackgroundResource(R.drawable.info_window_bg_left);
		}
		infoWindow.setLayoutParams(lp);
		fullScreenOverlay.setVisibility(View.VISIBLE);
	}
	
	private void hideInfoWindow() {
		if (previousCameraPosition != null)
			map.animateCamera(CameraUpdateFactory.newCameraPosition(previousCameraPosition));
		previousCameraPosition = null;
		// Hide the info window (a fade out would be nicer...)
		fullScreenOverlay.setVisibility(View.GONE);
	}
	
    /*
     * -- end Part 2
     */

    /* -----------------------------------------------------------------------
     * Part 3: Turn the lights down low
     * A spotlight effect for drawing focus to the content.
     * 
     */
	private void setupPart3() {
		fullScreenOverlay.setBackgroundResource(R.drawable.spotlight_gradient);
		spotDrawable = (GradientDrawable) fullScreenOverlay.getBackground();
		spotAnimation = new SpotlightAnimation(0, 0, 0, 0);
		spotAnimation.setDuration(400);
	}
	
	private void showSpotlight(int startX, int startY, int endX, int endY) {
		// Show the spotlight first at the start position
		final float cx = startX / (float) fullScreenOverlay.getWidth();
		final float cy = startY / (float) fullScreenOverlay.getHeight();
		setSpotlight(cx, cy);
		// Configure and start the animation 
		spotAnimation.set(startX, startY, endX, endY);
		fullScreenOverlay.startAnimation(spotAnimation);
	}
	
	private void setSpotlight(float cx, float cy) {
	    spotDrawable.setGradientCenter(cx, cy);
	    // The following line is needed for Android 2.2 to flag it's state
	    // as dirty and pick up the new gradient center set above.
	    spotDrawable.setGradientType(GradientDrawable.RADIAL_GRADIENT);
	}
	
	public class SpotlightAnimation extends Animation {
	    float cx0, cy0, dx, dy;
	    public SpotlightAnimation(final int startX, final int startY,
	            final int endX, final int endY) {
	        set(startX, startY, endX, endY);
	    }
	    public void set(final int startX, final int startY,
	            final int endX, final int endY) {
	        final float w = fullScreenOverlay.getWidth();
	        final float h = fullScreenOverlay.getHeight();
	        cx0 = (float) startX / w;
	        cy0 = (float) startY / h;
	        dx = (endX / w) - cx0;
	        dy = (endY / h) - cy0;
	    }
	    @Override
	    protected void applyTransformation(final float interpTime, final Transformation t) {
	        final float cx = cx0 + interpTime * dx;
	        final float cy = cy0 + interpTime * dy;
	        setSpotlight(cx, cy);
	    }
	    @Override
	    public boolean willChangeTransformationMatrix() { return false;  }
	    @Override
	    public boolean willChangeBounds() { return false; }
	}
	
    /*
     * -- end Part 3
     */

}
