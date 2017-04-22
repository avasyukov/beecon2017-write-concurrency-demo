import argparse
import requests
from multiprocessing import Pool, TimeoutError
from multiprocessing.dummy import Pool as ThreadPool

def make_request(api):
    url = base_url + args.api
    response = requests.post(url, data=payload, auth=authentication)
    print("%s: %s" % (api, response.status_code))

payload = {}
authentication = ('admin', 'admin')
base_url = 'http://localhost:8080/alfresco/s/write_concurrency_demo/'

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Demo load tool')
    parser.add_argument('-a', '--api', required=True, help='API name')
    parser.add_argument('-c', '--concurrency', required=True, help='Concurrency level')
    #parser.add_argument('-r', '--requests', required=True, help='Requests per thread')
    args = parser.parse_args()

    pool = ThreadPool(processes=int(args.concurrency))
    pool.map(make_request, [args.api for i in range(0,int(args.concurrency))])
    pool.close()
    pool.join()
