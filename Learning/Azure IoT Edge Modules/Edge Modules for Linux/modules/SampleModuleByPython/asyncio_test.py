import asyncio
import time


async def f1():
  print('F1 start, wait 2 sec...')
  
  await asyncio.sleep(2)
  # time.sleep(2)

  print('F1 complete!')

async def f2():
  print('F2 start, wait 1 sec...')
  
  await asyncio.sleep(1)
  # time.sleep(1)

  print('F2 complete!')


if __name__ == "__main__":
  asyncio..run(f1())
  asyncio.run(f2())
  print('main complete!')