Renderer costruito per gestire il rendering di una cella di dataset facendo sì che si possa inserire nella prima cella la checkbox per selezionare una o più righe.

In questa cartella oltre alla CE è presente un fragment apposito, riutilizzabile così com'è per poter gestire la selezione multipla e le action esistenti create sul dataset interessato.

Sono anche presenti una serie di immagini di documentazione del back office che possono tornare utili.

!!! ATTENZIONE !!!
Il JS del fragment "checkbox-and-actions" va comunque modificato leggermente per adattarlo alle actions che ci saranno nel progetto interessato.
La modifica andrà fatta nel JS e andranno inserite le azioni del dataset che ci interessa. (nella forma: [objectAction name corretto]: 'Label')

const ACTION_LABELS = {
    markAsCompleted: 'Segna come Completato',
    resetToNew: 'Segna come Nuovo',
};

NB: se non si conosce il nome della action si può debuggare inserendo:
console.log('BULK RENDERER itemData.actions:', actions);
La CE ha già all'interno quel log.