30000 constant km_change \ определяем нашу цену

: km_clan_teleport 
	list> drop                             \ раскроем список и уберем лишнее
	player@ "ClanId" p@ = if               \ игрок принадлежит к этому же клану?
		s>coords drop                          \ преобразуем координаты и дропаем флаг
		100 coords-randomize jump "You moved by leader" show exit \ телепортируем, показываем сообщение
	then
;

: user_km-all-to-me 
	clan-leader? 0 < if "You not clan leader" show exit then 
	km_change adena_pay drop               \ проверка и выплаты денег
	loc@ coords>s                          \ получаем текущие координаты лидера и преобразуем их в строку
	player@ "ClanId" p@ 2 >list            \ получаем ID клана и все упаковываем в список
	"km_clan_teleport" p-do-players        \ выполняем команду для всех игроков онлайн
;
