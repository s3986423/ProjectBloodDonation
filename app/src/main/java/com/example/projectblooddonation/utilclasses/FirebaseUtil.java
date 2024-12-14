package com.example.projectblooddonation.utilclasses;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseUtil {
    public static void addMockDonationSites() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        List<Map<String, Object>> sites = new ArrayList<>();

        // Mock donation sites
        sites.add(createSite("District 1 Donation Center", "123 Nguyen Hue Street, District 1, Ho Chi Minh City",
                10.7769, 106.7009, Arrays.asList("A+", "O-", "B+"),
                Arrays.asList("rAskSWfRVtXQ3ksWR3GaMyuz4282", "phvAfgP5SKdxSPGfSh0e8qQujB22")));
        sites.add(createSite("District 3 Blood Bank", "45 Vo Van Tan, District 3, Ho Chi Minh City",
                10.7843, 106.6937, Arrays.asList("AB-", "O+", "A+"),
                Arrays.asList("eNxCuxzjC3dfhkX3YHMTgTVhOT03", "GTkpEA3kDabEqYJM495a8gFvJx83")));
        sites.add(createSite("District 7 Health Center", "67 Phu My Hung, District 7, Ho Chi Minh City",
                10.7388, 106.7094, Arrays.asList("B+", "O-", "AB+"),
                Arrays.asList("ALabsrJisqMlsAZLu5tWU4esUsi1", "LXGva9zZ7OfYX2xIauFSDyZqQs83")));

        // Add sites to Firestore
        for (Map<String, Object> site : sites) {
            db.collection("donation_sites").add(site)
                    .addOnSuccessListener(documentReference ->
                            Log.d("Firestore", "Added mock site with ID: " + documentReference.getId()))
                    .addOnFailureListener(e ->
                            Log.e("Firestore", "Error adding mock site", e));
        }
    }

    private static Map<String, Object> createSite(String name, String address, double latitude, double longitude,
                                           List<String> requiredBloodTypes, List<String> registeredDonors) {
        Map<String, Object> site = new HashMap<>();
        site.put("name", name);
        site.put("address", address);
        site.put("location", new GeoPoint(latitude, longitude)); // Using Firestore GeoPoint for location
        site.put("requiredBloodTypes", requiredBloodTypes);      // List of required blood types
        site.put("registeredDonors", registeredDonors);          // List of donor IDs
        return site;
    }

}
