package com.matthewkruk.diseasetracker;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.subscriptions.Subscriptions;


public class RxFirebase {

    private enum EventType {
        CHILD_ADDED, CHILD_CHANGED, CHILD_REMOVED, CHILD_MOVED
    }

    /**
     * Essentially a struct so that we can pass all children events through as a single object.
     */
    public static class FirebaseChildEvent {
        public DataSnapshot snapshot;
        public EventType eventType;
        public String prevName;

        FirebaseChildEvent(DataSnapshot snapshot, EventType eventType, String prevName) {
            this.snapshot = snapshot;
            this.eventType = eventType;
            this.prevName = prevName;
        }
    }

    public static Observable<FirebaseChildEvent> observeChildren(final Query ref) {
        return Observable.create(new Observable.OnSubscribe<FirebaseChildEvent>() {

            @Override
            public void call(final Subscriber<? super FirebaseChildEvent> subscriber) {
                final ChildEventListener listener = ref.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String prevName) {
                        subscriber.onNext(new FirebaseChildEvent(dataSnapshot, EventType.CHILD_ADDED, prevName));
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String prevName) {
                        subscriber.onNext(new FirebaseChildEvent(dataSnapshot, EventType.CHILD_CHANGED, prevName));
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        subscriber.onNext(new FirebaseChildEvent(dataSnapshot, EventType.CHILD_REMOVED, null));
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String prevName) {
                        subscriber.onNext(new FirebaseChildEvent(dataSnapshot, EventType.CHILD_MOVED, prevName));
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Notify Subscriber
                        subscriber.onError(error.toException());
                    }
                });

                // When the subscription is cancelled, remove the listener
                subscriber.add(Subscriptions.create(() -> ref.removeEventListener(listener)));
            }
        });
    }

    private static Func1<FirebaseChildEvent, Boolean> makeEventFilter(final EventType eventType) {
        return (ev -> ev.eventType == eventType);
    }

    public static Observable<FirebaseChildEvent> observeChildAdded(Query ref) {
        return observeChildren(ref).filter(makeEventFilter(EventType.CHILD_ADDED));
    }

    public static Observable<FirebaseChildEvent> observeChildChanged(Query ref) {
        return observeChildren(ref).filter(makeEventFilter(EventType.CHILD_CHANGED));
    }

    public static Observable<FirebaseChildEvent> observeChildMoved(Query ref) {
        return observeChildren(ref).filter(makeEventFilter(EventType.CHILD_MOVED));
    }

    public static Observable<FirebaseChildEvent> observeChildRemoved(Query ref) {
        return observeChildren(ref).filter(makeEventFilter(EventType.CHILD_REMOVED));
    }

    public static Observable<DataSnapshot> observe(final Query ref) {

        return Observable.create(new Observable.OnSubscribe<DataSnapshot>() {

            @Override
            public void call(final Subscriber<? super DataSnapshot> subscriber) {
                final ValueEventListener listener = ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        subscriber.onNext(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Notify Subscriber
                        subscriber.onError(error.toException());
                    }
                });

                // When the subscription is cancelled, remove the listener
                subscriber.add(Subscriptions.create(() -> ref.removeEventListener(listener)));
            }
        });
    }

    /**
     * @param ref
     * @return
     */
    public static Observable<DataSnapshot> observeSingle(final Query ref) {

        return Observable.create(new Observable.OnSubscribe<DataSnapshot>() {

            @Override
            public void call(final Subscriber<? super DataSnapshot> subscriber) {
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        subscriber.onNext(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Notify Subscriber
                        subscriber.onError(error.toException());
                    }
                });
            }
        });
    }


    /**
     * @param dbRef
     * @param object
     * @return
     */
    public static Observable<Task<Void>> observePush(final DatabaseReference dbRef, Object object) {
        return Observable.create(new Observable.OnSubscribe<Task<Void>>() {
            @Override
            public void call(Subscriber<? super Task<Void>> subscriber) {
                dbRef.push().setValue(object)
                        .addOnCompleteListener(subscriber::onNext)
                        .addOnFailureListener(subscriber::onError);
            }
        });
    }

    /**
     * @param dbRef
     * @param object
     * @return
     */
    public static Observable<Task<Void>> observeUpdate(final DatabaseReference dbRef, Object object) {
        return Observable.create(new Observable.OnSubscribe<Task<Void>>() {
            @Override
            public void call(Subscriber<? super Task<Void>> subscriber) {
                dbRef.setValue(object)
                        .addOnCompleteListener(subscriber::onNext)
                        .addOnFailureListener(subscriber::onError);
            }
        });
    }
}