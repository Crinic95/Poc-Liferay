Renderer costruito per gestire il rendering di una cella di dataset in 2 modalità: 
	- modalità titolo strutturato
	- testo semplice 
a seconda dei dati presenti in "itemData". 

itemData: l'intero record della riga
value: il valore della cella corrente

Se in itemData esiste almeno uno dei 2 (detailTitle o cardTitle) allora usa la modalità cella strutturata altrimenti span semplice.