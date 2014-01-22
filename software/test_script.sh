#!/bin/bash


for (( i=1; i <= $1; i++ )); do
	if [[ $i == 1 ]]; then
		echo "1/$1 vez"
	else
		echo "$i/$1 veces"
	fi
	sleep $2
done

