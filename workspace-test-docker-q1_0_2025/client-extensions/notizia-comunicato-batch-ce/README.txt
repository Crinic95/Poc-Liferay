Questo pacchetto copre SOLO il modello:
- object folder
- object definitions
- relazione 1:N

I DATI (object entries) NON sono inclusi nella CE.
Vanno importati dopo, usando i CSV:

- notizia-comunicato_import.csv
- notizia-comunicato_asset-import.csv

Ordine consigliato:
1. deploy CE modello
2. verificare creazione Objects e relazione
3. generare i due CSV reali (5_build_notizia_comunicato_csv_imports.py)
4. importare prima NotiziaComunicato (csv)
5. importare poi NotiziaComunicatoAsset (csv)