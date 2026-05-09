package com.example.tutorialgame.cloud;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Map;

/**
 * מחלקה אבסטרקטית המהווה בסיס לכל המסמכים הנטענים מ-Firestore.
 * היא מנהלת את תהליך הטעינה האסינכרוני, תומכת במצב אופליין, מטפלת בשגיאות,
 * ומחייבת את המחלקות היורשות לממש רק את לוגיקת פירוק הנתונים הספציפית להן.
 */
public abstract class BaseDocument {
    protected final Runnable onFinishedLoading;
    protected final DocumentReference docRef;
    public BaseDocument(DocumentReference docRef, Runnable onFinishedLoading) {
        this.docRef = docRef;
        this.onFinishedLoading = onFinishedLoading;
    }

    /**
     * מתודה אבסטרקטית. כל מחלקה יורשת חייבת לממש אותה
     * כדי לפרסר את הנתונים הרלוונטיים לה מתוך המפה שהתקבלה מהמסמך.
     * @param data המפה המכילה את כל נתוני המסמך הפנימי (למשל, כל מה שבתוך "profile").
     */
    protected abstract void parseData(@NonNull Map<String, Object> data);

    /**
     * מתודה אבסטרקטית. כל מחלקה יורשת חייבת לספק את שם המפתח שלה במסמך הראשי.
     * @return שם המסמך הפנימי (למשל, "cosmetic", "profile").
     */
    @NonNull
    protected abstract String getDocName();

    /**
     * מתודה זו מנהלת את כל תהליך הטעינה מ-Firestore, עם תמיכה במצב אופליין.
     * היא שולפת את המסמך (מהשרת או מהמטמון), מטפלת בשגיאות, ובמקרה של הצלחה,
     * קוראת למתודה האבסטרקטית parseData כדי שהמחלקה היורשת תטפל בנתונים.
     * @param listener ה-listener הראשי של UserDataManager לדיווח על כשלים.
     */
    @SuppressWarnings("unchecked")
    public void loadAndCache(UserDataManager.OnDataLoadedListener listener) {
        // אנחנו לא מציינים מקור (Source), ולכן Firestore ינסה להביא מהשרת,
        // ואם ייכשל, יחזיר אוטומטית מהמטמון המקומי.
        docRef.get().addOnCompleteListener(task -> {
            try {
                // בדיקה אם ה-Task הצליח. זה יכול להצליח גם אם הנתונים מהמטמון.
                if (task.isSuccessful()) {
                    DocumentSnapshot snapshot = task.getResult();

                    if (snapshot != null && snapshot.exists()) {
                        // המסמך קיים (בין אם מהשרת או מהמטמון)
                        // שלוף את המפה הפנימית לפי השם שהמחלקה היורשת מספקת
                        Map<String, Object> specificData = (Map<String, Object>) snapshot.get(getDocName());

                        if (specificData != null) {
                            // קרא למתודה שכל מחלקה יורשת מממשת כדי לפרסר את הנתונים
                            parseData(specificData);

                            // (אופציונלי) הדפסה ללוג כדי לדעת מאיפה הגיעו הנתונים
                            if (snapshot.getMetadata().isFromCache()) {
                                Log.d("BaseDocument", getDocName() + " data loaded from cache.");
                            } else {
                                Log.d("BaseDocument", getDocName() + " data loaded from server.");
                            }
                            
                            // *** התיקון: דיווח על הצלחה ***
                            if (listener != null) {
                                listener.onDataLoadSuccess();
                            }
                        } else {
                            // המפה הספציפית (למשל, "profile") לא קיימת במסמך
                            throw new Exception("Document part '" + getDocName() + "' not found.");
                        }
                    } else {
                        throw new Exception("User document does not exist.");
                    }
                } else {
                    // ה-Task נכשל לחלוטין (לא שרת, לא מטמון) - זו שגיאה חמורה
                    throw task.getException() != null ? task.getException() : new Exception("Failed to load user document.");
                }
            } catch (Exception e) {
                // אם כל שגיאה התרחשה (המרה, null, וכו'), דווח עליה
                Log.e("BaseDocument", "Error loading document part: " + getDocName(), e);
                if (listener != null) {
                    listener.onDataLoadFailed();
                }
            } finally {
                // בכל מקרה, דווח שהתהליך הסתיים כדי שהמונה הכללי יוכל להמשיך לעלות
                if (onFinishedLoading != null) {
                    onFinishedLoading.run();
                }
            }
        });
    }
}
