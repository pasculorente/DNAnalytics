#!/bin/bash

echo "error" >&2

sleep 5
if [[ $# < 2 ]]; then
	sleep_time=2
else
	sleep_time=$2
fi
if [[ $# < 1 ]]; then
	steps=10
else
	steps=$1
fi

for (( i=1; i <= $steps; i++ )); do
	if [[ $i == 1 ]]; then
		echo "1/$steps vez"
	else
		echo "$i/$steps veces"
	fi
	sleep $sleep_time
done



